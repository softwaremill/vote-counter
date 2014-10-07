package com.softwaremill.votecounter.jdd

import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.io.Source

class JddHttpAgendaReader extends JddAgendaReader with LazyLogging {

  val AgendaJsonUrl = "http://14.jdd.org.pl/agenda/lectures/json"

  override protected def json: String = {
    logger.info(s"Retrieving JDD agenda from URL: $AgendaJsonUrl")
    Source.fromURL(AgendaJsonUrl).mkString
  }
}
