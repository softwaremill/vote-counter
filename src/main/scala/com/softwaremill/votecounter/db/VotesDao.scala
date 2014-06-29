package com.softwaremill.votecounter.db

import org.joda.time.DateTime
import com.softwaremill.votecounter.h2.SQLDatabase

case class Vote(id: String, deviceId: Int, castedAt: DateTime, positive: Boolean)

class VotesDao(val database: SQLDatabase) extends DBSchema {

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

  def insertIfNew(vote: Vote) : Vote = {
    db.withTransaction { implicit session =>
      val voteInDbOpt = votes.filter(_.id === vote.id).firstOption
      if (voteInDbOpt.isEmpty) {
        votes.insert(vote)
        vote
      } else {
        voteInDbOpt.get
      }
    }
  }

  def count(): Int = {
    db.withSession { implicit session =>
      votes.size.run
    }
  }

  def findAllByRoom(): Map[Room, List[Vote]] = {
    db.withSession { implicit session =>
      val query = for {
        v <- votes
        d <- devices if v.deviceId === d.id
        r <- rooms if d.roomId === r.id
      } yield (v, r)

      query.list groupBy { case (v, r) => r} map {
        case (key, votesWithRooms) =>
          (key, votesWithRooms map { case (vote, room) => vote})
      }
    }
  }

  def truncate() = {
    db.withSession { implicit session =>
      votes.delete
    }
  }
}
