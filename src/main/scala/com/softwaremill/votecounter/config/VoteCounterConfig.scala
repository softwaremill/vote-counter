package com.softwaremill.votecounter.config

import com.typesafe.config.{ConfigFactory, Config}
import com.softwaremill.thegarden.lawn.config.ConfigWithDefaults
import com.softwaremill.thegarden.web.jetty.WebServerConfig
import org.joda.time.{DateTimeZone, LocalDate}

trait BaseConfig extends ConfigWithDefaults {

  def rootConfig: Config

  lazy val embeddedDataDir: String = getString("db.data-dir", "./data")
}

class VoteCounterConfig extends BaseConfig with WebServerConfig {

  override def rootConfig = ConfigFactory.load()

  lazy val conferenceDate = new LocalDate(2014, 7, 5)

  lazy val conferenceTimeZone = DateTimeZone.forID("Europe/Warsaw")
}
