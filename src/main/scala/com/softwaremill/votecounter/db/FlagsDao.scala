package com.softwaremill.votecounter.db

import com.softwaremill.votecounter.h2.SQLDatabase

case class Flag(flagId: String, value: String)

class FlagsDao(val database: SQLDatabase) extends DBSchema {

  import database.driver.simple._
  import database._

  def find(flagId: String) = {
    db.withSession { implicit session =>
      flags.filter(_.flagId === flagId).firstOption
    }
  }

  def set(flag: Flag) = {
    db.withTransaction { implicit session =>
      val filterQuery = flags.filter(_.flagId === flag.flagId)
      val flagInDbOpt = filterQuery.firstOption
      flagInDbOpt match {
        case Some(_) => filterQuery.update(flag)
        case None => flags.insert(flag)
      }
    }
  }

  def count() : Int = {
    db.withSession { implicit session =>
      flags.list.length
    }
  }

}
