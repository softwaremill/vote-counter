package com.softwaremill.votecounter.testutil

import org.scalatest.{Suite, BeforeAndAfterEach, BeforeAndAfterAll, FlatSpec}
import scala.slick.jdbc.StaticQuery
import com.softwaremill.votecounter.h2.{DBInitializer, SQLDatabase}

trait SQLSupport extends BeforeAndAfterAll with BeforeAndAfterEach {
  this: Suite =>

  val DBNamePrefix = "vc_test"

  private val connectionString =
    s"jdbc:h2:mem:${DBNamePrefix}_${this.getClass.getSimpleName};DB_CLOSE_DELAY=-1"

  val sqlDatabase = SQLDatabase.createEmbedded(connectionString)
  private val dbInitializer = new DBInitializer(sqlDatabase)

  override protected def beforeAll() {
    super.beforeAll()
    createAll()
  }

  def clearData() {
    dropAll()
    createAll()
  }

  override protected def afterAll() {
    super.afterAll()
    dropAll()
    sqlDatabase.close()
  }

  private def dropAll() {
    sqlDatabase.db.withSession { implicit session =>
      StaticQuery.updateNA("DROP ALL OBJECTS").execute
    }
  }

  private def createAll() {
    dbInitializer.initializeAndBlock()
  }

}
