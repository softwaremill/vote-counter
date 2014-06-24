package com.softwaremill.votecounter.db

import com.softwaremill.votecounter.h2.SQLDatabase
import org.joda.time.DateTime

trait DBSchema {
  protected val database: SQLDatabase

  import database._
  import database.driver.simple._

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

    // TODO add room id

    def * = (id.?, key, name) <>(Device.tupled, Device.unapply)

  }

  protected lazy val devices = TableQuery[Devices]

  val VotesTableName = "Votes"

  class Votes(tag: Tag) extends Table[Vote](tag, VotesTableName) {

    def id = column[String]("vote_id", O.PrimaryKey, O.NotNull)

    def deviceId = column[Int]("device_id", O.NotNull)

    def castedAt = column[DateTime]("casted_at", O.NotNull)

    def positive = column[Boolean]("positive", O.NotNull)

    override def * = (id, deviceId, castedAt, positive) <>(Vote.tupled, Vote.unapply)

    def device = foreignKey("device_id", deviceId, devices)(_.id)
  }

  protected lazy val votes = TableQuery[Votes]

  val RoomsTableName = "Rooms"

  class Rooms(tag: Tag) extends Table[Room](tag, RoomsTableName) {

    def id = column[String]("room_id", O.PrimaryKey, O.NotNull)

    def name = column[String]("name", O.NotNull)

    override def * = (id, name) <>(Room.tupled, Room.unapply)
  }

  protected lazy val rooms = TableQuery[Rooms]

  val TalksTableName = "Talks"

  class Talks(tag: Tag) extends Table[Talk](tag, TalksTableName) {

    def id = column[String]("talk_id", O.PrimaryKey, O.NotNull)

    def roomId = column[String]("room_id", O.NotNull)

    def title = column[String]("title", O.NotNull)

    def startsAt = column[DateTime]("starts_at", O.NotNull)

    def endsAt = column[DateTime]("ends_at", O.NotNull)

    def overrideVoteStartAt = column[DateTime]("overrride_vote_starts_at", O.Nullable)

    def overrideVoteEndsAt = column[DateTime]("override_vote_ends_at", O.Nullable)

    override def * = (id, roomId, title, startsAt, endsAt, overrideVoteStartAt.?, overrideVoteEndsAt.?) <>(Talk.tupled, Talk.unapply)
  }

  protected lazy val talks = TableQuery[Talks]

}


