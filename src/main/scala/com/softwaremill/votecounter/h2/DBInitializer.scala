package com.softwaremill.votecounter.h2

import com.softwaremill.votecounter.db._
import scala.slick.jdbc.meta.MTable
import org.joda.time.DateTime
import com.softwaremill.votecounter.db.Vote
import com.softwaremill.votecounter.db.Device
import scala.Some
import com.softwaremill.votecounter.infrastructure.AppFlags
import com.softwaremill.votecounter.confitura.{TalksProvider, RoomsProvider}


class TestDataPopulator(deviceDao: DevicesDao, voteDao: VotesDao, appFlags: AppFlags) {

  def populateWithTestData() {
    if (!appFlags.isTestDataInserted) {
      val devices = Seq(Device(key = "123", name = "foo", deviceId = Some(1), roomId = "dzem"))

      val votes = Seq(
        Vote(positive = true, castedAt = DateTime.now().minusDays(1), deviceId = 1, id = "123"),
        Vote(positive = true, castedAt = DateTime.now().minusDays(2), deviceId = 1, id = "311"),
        Vote(positive = false, castedAt = DateTime.now().minusDays(3), deviceId = 1, id = "41a")
      )

      for (device <- devices)
        deviceDao.insert(device)

      for (vote <- votes)
        voteDao.insert(vote)

      appFlags.flagTestDataInserted()
    }
  }
}

class ConferenceDataInitializer(roomsProvider: RoomsProvider, roomsDao: RoomsDao,
                                talksProvider: TalksProvider, talksDao: TalksDao) {

  def initializeAndBlock() {
    for (room <- roomsProvider.rooms) {
      roomsDao.insert(room)
    }

    for (talk <- talksProvider.talks) {
      talksDao.insert(talk)
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
