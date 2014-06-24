package com.softwaremill.votecounter.db

import org.joda.time.DateTime
import com.softwaremill.votecounter.h2.SQLDatabase

case class Talk(talkId: String, roomId: String, title: String,
                startsAt: DateTime, endsAt: DateTime,
                overrideVoteStartsAt: Option[DateTime], overrideVoteEndsAt: Option[DateTime])


class TalksDao(protected val database: SQLDatabase) extends DBSchema {

  import database._
  import database.driver.simple._

  def insert(talk: Talk) = {
    db.withSession { implicit session =>
      talks.insert(talk)
    }
  }

}