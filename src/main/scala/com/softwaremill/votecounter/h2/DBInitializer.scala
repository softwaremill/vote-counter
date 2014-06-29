package com.softwaremill.votecounter.h2

import com.softwaremill.votecounter.confitura.{DevicesProvider, RoomsProvider, TalksProvider}
import com.softwaremill.votecounter.db._
import com.softwaremill.votecounter.infrastructure.AppFlags
import org.joda.time.DateTime

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
                                talksProvider: TalksProvider, talksDao: TalksDao,
                                devicesProvider: DevicesProvider, devicesDao: DevicesDao,
                                appFlags: AppFlags) {

  def initializeAndBlock() {
    if (!appFlags.isConferenceDataInitialized) {

      for (room <- roomsProvider.rooms) {
        roomsDao.insert(room)
      }

      for (talk <- talksProvider.talks) {
        talksDao.insert(talk)
      }

      for (device <- devicesProvider.devices) {
        devicesDao.insert(device)
      }

      appFlags.flagConferenceDataInserted()
    }
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
