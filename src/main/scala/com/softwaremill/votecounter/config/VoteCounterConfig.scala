package com.softwaremill.votecounter.config

import com.typesafe.config.{ConfigFactory, Config}
import com.softwaremill.thegarden.lawn.config.ConfigWithDefaults
import com.softwaremill.thegarden.web.jetty.WebServerConfig
import org.joda.time.{Period, DateTimeZone, LocalDate}

trait BaseConfig extends ConfigWithDefaults {

  import BaseConfig._

  def rootConfig: Config

  lazy val embeddedDataDir = getString("vote-counter.db.data-dir", "./data")

  val sslKeystorePassword : Option[String] = getOptionalString(SslKeystorePasswordKey)
  val sslEnabled : Boolean = getBoolean("vote-counter.web.ssl-enabled", default = false)
  val webCacheFiles : Boolean = getBoolean("vote-counter.web.cache-files", default = true)
}

trait VoteAggregatorConfig {
  val sessionsDelay : Period = Period.minutes(10)
}

object BaseConfig {

  val SslKeystorePasswordKey = "vote-counter.web.keystore-password"
}

class VoteCounterConfig extends BaseConfig with WebServerConfig with VoteAggregatorConfig {

  override def rootConfig = ConfigFactory.load()

  lazy val conferenceDate = new LocalDate(2014, 7, 5)

  lazy val conferenceTimeZone = DateTimeZone.forID("Europe/Warsaw")
}
