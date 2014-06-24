package com.softwaremill.votecounter.db

import com.softwaremill.votecounter.h2.SQLDatabase

case class Room(roomId: String, name: String)

class RoomsDao(protected val database: SQLDatabase) extends DBSchema {

  import database._
  import database.driver.simple._

  def insert(room: Room) = {
    db.withSession { implicit session =>
      rooms.insert(room)
    }
  }

}