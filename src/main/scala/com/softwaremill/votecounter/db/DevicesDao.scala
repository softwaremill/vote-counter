package com.softwaremill.votecounter.db

import com.softwaremill.votecounter.h2.SQLDatabase

case class Device(deviceId: Option[Int], key: String, name: String, roomId: String)

class DevicesDao(val database: SQLDatabase) extends DBSchema {

  import database.driver.simple._
  import database._

  def findAll() = {
    db.withSession { implicit session =>
      devices.list
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
