package com.softwaremill.votecounter.h2

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Paths, Files}

import com.softwaremill.votecounter.common._
import com.softwaremill.votecounter.db._
import com.softwaremill.votecounter.infrastructure.AppFlags
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.joda.time.DateTime

import scala.io.Source
import scala.slick.jdbc.meta.MTable

class TestDataPopulator(voteDao: VotesDao, appFlags: AppFlags) {

  def populateWithTestData() {
    if (!appFlags.isTestDataInserted) {
      val votes = Seq(
        Vote(positive = true, castedAt = DateTime.now().minusDays(1), deviceId = 1, id = "123"),
        Vote(positive = true, castedAt = DateTime.now().minusDays(2), deviceId = 1, id = "311"),
        Vote(positive = false, castedAt = DateTime.now().minusDays(3), deviceId = 1, id = "41a")
      )

      for (vote <- votes)
        voteDao.insert(vote)

      appFlags.flagTestDataInserted()
    }
  }
}

class ConferenceDataInitializer(roomsProvider: RoomsProvider, roomsDao: RoomsDao,
                                agendaProvider: AgendaProvider, talksDao: TalksDao,
                                devicesProvider: DevicesProvider, devicesDao: DevicesDao,
                                appFlags: AppFlags, agendaVersionAccessor: AgendaVersionAccessor) extends LazyLogging {

  def initializeAndBlock() {
    updateTalksIfAgendaIsOutdated()

    if (!appFlags.isConferenceDataInitialized) {
      for (room <- roomsProvider.rooms) {
        roomsDao.insert(room)
      }

      for (device <- devicesProvider.devices) {
        devicesDao.insert(device)
      }

      appFlags.flagConferenceDataInserted()
    }
  }

  private[h2] def updateTalksIfAgendaIsOutdated() {
    val latestAgenda = agendaProvider.read()

    if (!appFlags.isConferenceDataInitialized) {
      logger.info("Conference data not initialized yet, so the talks will be updated")
      updateTalks(latestAgenda)
    } else {
      val currentAgendaVersion = agendaVersionAccessor.read()
      currentAgendaVersion match {
        case Some(version) if version < latestAgenda.version =>
          logger.info(s"Agenda is outdated (we have ${currentAgendaVersion.get}, latest version is ${latestAgenda.version}), so the talks will be updated")
          updateTalks(latestAgenda)
        case None =>
          logger.info("Conference data already initialized, but no agenda version present, so the talks won't be updated")
        case _ =>
          logger.info("Talks are up to date")
      }
    }
  }

  private def updateTalks(agenda: Agenda) {
    talksDao.replaceAll(agenda.talks)
    agendaVersionAccessor.write(agenda.version)
  }
}

class DBInitializer(protected val database: SQLDatabase) extends DBSchema {

  import database._
  import database.driver.simple._

  def initializeAndBlock() {
    createSchema()
  }

  def dropDb() = ???

  protected def createSchema() {
    def createTableIfDoesNotExist(name: String, ddl: database.driver.DDL) {
      db.withSession { implicit session =>
        if (MTable.getTables(name).list.isEmpty) {
          ddl.create
        }
      }
    }

    createTableIfDoesNotExist(FlagTableName, flags.ddl)
    createTableIfDoesNotExist(RoomsTableName, rooms.ddl)
    createTableIfDoesNotExist(DevicesTableName, devices.ddl)
    createTableIfDoesNotExist(TalksTableName, talks.ddl)
    createTableIfDoesNotExist(VotesTableName, votes.ddl)
  }

}

class AgendaVersionAccessor {

  val AgendaVersionFile = new File(".agenda_version")

  def read(): Option[String] = if (AgendaVersionFile.exists)
    Some(Source.fromFile(AgendaVersionFile).getLines().mkString)
  else
    None

  def write(version: String): Unit = {
    Files.write(Paths.get(AgendaVersionFile.getName), version.getBytes(StandardCharsets.UTF_8))
  }
}
