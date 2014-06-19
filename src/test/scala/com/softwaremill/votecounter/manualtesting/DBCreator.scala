package com.softwaremill.votecounter.manualtesting

import com.softwaremill.votecounter.db.DBSchema
import com.softwaremill.votecounter.infrastructure.Beans
import scala.slick.jdbc.meta.MTable

object DBCreatorRunner extends App {
  private val creator = new DBCreator()
  creator.createSchema()
}

class DBCreator extends DBSchema {
  val beans = Beans
  override val database = beans.database

  import database._
  import database.driver.simple._

  def createSchema() {

    db.withSession { implicit session =>
      if (MTable.getTables(DevicesTableName).list.isEmpty) {
        devices.ddl.create
      }
    }
  }
}
