package com.softwaremill.votecounter.db

import org.joda.time.DateTime
import com.softwaremill.votecounter.h2.SQLDatabase

case class Vote(voteId: String, deviceId: Int, castedAt: DateTime, positive: Boolean)

class VoteDao(val database: SQLDatabase) extends DBSchema {

  import database.driver.simple._
  import database._

  def findAll() = {
    db.withSession { implicit session =>
      votes.list
    }
  }

  def insert(vote: Vote) = {
    db.withSession { implicit session =>
      votes.insert(vote)
    }
  }

}