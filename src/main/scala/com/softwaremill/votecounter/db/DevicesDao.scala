package com.softwaremill.votecounter.db

import com.softwaremill.votecounter.h2.SQLDatabase
import org.joda.time.DateTime

case class Device(deviceId: Option[Int], key: String, name: String, roomId: String, lastSeen: Option[DateTime] = None)

class DevicesDao(val database: SQLDatabase) extends DBSchema {

  import database.driver.simple._
  import database._

  def findAll() = {
    db.withSession { implicit session =>
      devices.list
    }
  }

  def updateLastSeen(id: Int, lastSeen: DateTime) {
    db.withTransaction { implicit session =>
      val query = for (d <- devices.filter(_.id === id)) yield d.lastSeen
      query.update(lastSeen)
    }
  }

  def findHeartbeats() = {
    db.withSession { implicit session =>
      val query = for {
        d <- devices
        r <- rooms if r.id === d.roomId
      } yield (d.name, r.name, d.lastSeen.?)

      query.list.map {
        case (deviceName, roomName, maybeLastSeen) => {
          Map("device" -> deviceName, "room" -> roomName, "lastSeen" -> maybeLastSeen.map(_.getMillis).getOrElse(0))
        }
      }
    }
  }

  def findByKey(key: String): Option[Device] = {
    db.withSession { implicit session =>
      devices.filter(_.key === key).firstOption
    }
  }

  def insert(device: Device) = {
    db.withSession { implicit session =>
      devices.insert(device)
    }
  }

}
