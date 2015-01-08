package com.softwaremill.votecounter.db

import org.joda.time.DateTime
import com.softwaremill.votecounter.h2.SQLDatabase

case class Talk(talkId: String, roomId: String, title: String,
                startsAt: DateTime, endsAt: DateTime,
                overrideVoteStartsAt: Option[DateTime], overrideVoteEndsAt: Option[DateTime])

object Talk extends ((String, String, String, DateTime, DateTime, Option[DateTime], Option[DateTime]) => Talk) {
  def apply(talkId: String, roomId: String, title: String,
            startsAt: DateTime, endsAt: DateTime): Talk =
    Talk(talkId, roomId, title, startsAt, endsAt, None, None)


}

class TalksDao(protected val database: SQLDatabase) extends DBSchema {

  import database._
  import database.driver.simple._

  def insert(talk: Talk) = {
    db.withSession { implicit session =>
      talks.insert(talk)
    }
  }

  def findAllByRoom(): Map[Room, List[Talk]] = {
    db.withSession { implicit session =>
      val byRoomId = talks.list.groupBy(_.roomId)
      val roomsDict: Map[String, Room] = rooms.list.map(room => (room.roomId, room)).toMap

      byRoomId.map { case (roomId, talk) => (roomsDict(roomId), talk)}
    }
  }

  def replaceAll(newTalks: Seq[Talk]): Unit = {
    db.withTransaction { implicit session =>
      talks.delete
      newTalks.foreach(talks.insert)
    }
  }
}