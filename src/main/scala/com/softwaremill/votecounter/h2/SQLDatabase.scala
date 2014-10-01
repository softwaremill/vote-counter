package com.softwaremill.votecounter.h2

import scala.slick.driver.JdbcProfile
import javax.sql.DataSource
import com.typesafe.scalalogging.slf4j.{LazyLogging => Logging}
import org.joda.time.{LocalDateTime, LocalTime, DateTimeZone, DateTime}
import com.googlecode.flyway.core.Flyway
import com.mchange.v2.c3p0.{ComboPooledDataSource, DataSources}
import java.io.File
import scala.slick.jdbc.JdbcBackend._
import com.softwaremill.votecounter.config.BaseConfig
import java.sql.Time

case class SQLDatabase(db: scala.slick.jdbc.JdbcBackend.Database,
                       driver: JdbcProfile,
                       ds: DataSource) extends Logging {

  import driver.simple._

  implicit val dateTimeColumnType = MappedColumnType.base[DateTime, java.sql.Timestamp](
    dt => new java.sql.Timestamp(dt.getMillis),
    t => new DateTime(t.getTime).withZone(DateTimeZone.UTC)
  )

  implicit val localTimeColumnType = MappedColumnType.base[LocalTime, java.sql.Time](
    dt => new Time(LocalDateTime.now().
      withTime(dt.getHourOfDay, dt.getMinuteOfHour, dt.getSecondOfMinute, dt.getMillisOfSecond).toDate.getTime),
    t => new LocalDateTime(t.getTime).toLocalTime
  )

  def updateSchema() {
    val flyway = new Flyway()
    flyway.setDataSource(ds)
    flyway.migrate()
  }

  def close() {
    DataSources.destroy(ds)
  }
}

object SQLDatabase extends Logging {
  def connectionString(config: BaseConfig): String = {
    val fullPath = new File(config.embeddedDataDir, "votecounter").getCanonicalPath
    logger.info(s"Using an embedded database, with data files located at: $fullPath")

    s"jdbc:h2:file:$fullPath"
  }

  def createEmbedded(config: BaseConfig): SQLDatabase = {
    createEmbedded(connectionString(config))
  }

  def createEmbedded(connectionString: String): SQLDatabase = {
    val ds = createConnectionPool(connectionString)
    val db = Database.forDataSource(ds)
    SQLDatabase(db, scala.slick.driver.H2Driver, ds)
  }

  private def createConnectionPool(connectionString: String) = {
    val cpds = new ComboPooledDataSource()
    cpds.setDriverClass("org.h2.Driver")
    cpds.setJdbcUrl(connectionString)
    cpds
  }
}