package com.softwaremill.votecounter.db

import com.softwaremill.votecounter.h2.SQLDatabase
import org.joda.time.DateTime

trait DBSchema {
  protected val database: SQLDatabase

  import database.driver.simple._
  import database._

  val FlagTableName = "flags"

  class Flags(tag: Tag) extends Table[Flag](tag, FlagTableName) {

    def flagId = column[String]("flag_id", O.PrimaryKey)

    def value = column[String]("value")

    def * = (flagId, value) <>(Flag.tupled, Flag.unapply)
  }

  protected lazy val flags = TableQuery[Flags]

  val DevicesTableName = "Devices"

  class Devices(tag: Tag) extends Table[Device](tag, DevicesTableName) {
    def id = column[Int]("device_id", O.PrimaryKey, O.AutoInc)

    // This is the primary key column
    def key = column[String]("key", O.NotNull)

    def name = column[String]("name", O.NotNull)

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id.?, key, name) <>(Device.tupled, Device.unapply)
  }

  protected lazy val devices = TableQuery[Devices]

  val VotesTableName = "Votes"

  class Votes(tag: Tag) extends Table[Vote](tag, VotesTableName) {

    def id = column[String]("vote_id", O.PrimaryKey)

    def deviceId = column[Int]("device_id", O.NotNull)

    def castedAt = column[DateTime]("casted_at", O.NotNull)

    def positive = column[Boolean]("positive", O.NotNull)

    override def * = (id, deviceId, castedAt, positive) <>(Vote.tupled, Vote.unapply)

    def device = foreignKey("device_id", deviceId, devices)(_.id)
  }

  protected lazy val votes = TableQuery[Votes]

}
