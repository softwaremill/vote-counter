package com.softwaremill.votecounter.h2

import com.softwaremill.votecounter.db._
import scala.slick.jdbc.meta.MTable
import org.joda.time.DateTime
import com.softwaremill.votecounter.db.Vote
import com.softwaremill.votecounter.db.Device
import scala.Some


class TestDataPopulator(deviceDao: DeviceDao, voteDao: VoteDao, appFlags: AppFlags) {

  def populateWithTestData() {
    if (!appFlags.isTestDataInserted) {
      val devices = Seq(Device(key = "123", name = "foo", deviceId = Some(1)))

      val votes = Seq(
        Vote(positive = true, castedAt = DateTime.now().minusDays(1), deviceId = 1, voteId = "123"),
        Vote(positive = true, castedAt = DateTime.now().minusDays(2), deviceId = 1, voteId = "311"),
        Vote(positive = false, castedAt = DateTime.now().minusDays(3), deviceId = 1, voteId = "41a")
      )

      for (device <- devices)
        deviceDao.insert(device)

      for (vote <- votes)
        voteDao.insert(vote)

      appFlags.flagTestDataInserted()
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

    createTableIfDoesNotExist(DevicesTableName, devices.ddl)
    createTableIfDoesNotExist(VotesTableName, votes.ddl)
    createTableIfDoesNotExist(FlagTableName, flags.ddl)
  }

}