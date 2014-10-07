package com.softwaremill.votecounter.confitura

import com.softwaremill.votecounter.infrastructure.Beans
import org.scalatest.{ShouldMatchers, FlatSpec}
import com.typesafe.scalalogging.slf4j.LazyLogging

class ConfituraAgendaReaderSpec extends FlatSpec with ShouldMatchers with LazyLogging {

  private val agenda = new ConfituraAgendaReader(Beans.config.conferenceDate, Beans.config.conferenceTimeZone).read()

  it should "read and parse the agenda3.json file" in {

    agenda.version shouldEqual "1.30.2"
  }

  it should "have more than 10 talks (sanity check)" in {
    val numberOfTalks = agenda.talks.length
    logger.debug(s"Agenda file has $numberOfTalks talks")
    numberOfTalks should be > 10
  }

  it should "produce an agenda without duplicates" in {
    agenda.verifyUniqueTalkIds()
  }
}

object RoomPrinter extends App {

  private val agenda = new ConfituraAgendaReader(Beans.config.conferenceDate, Beans.config.conferenceTimeZone).readAgendaFile()

  println(agenda.talks.map(talk => (talk.room, talk.roomId)).distinct.sorted.mkString("\n"))
}

object LongestTalkTitlePrinter extends App {

  private val agenda = new ConfituraAgendaReader(Beans.config.conferenceDate, Beans.config.conferenceTimeZone).read()

  println(agenda.talks.map(_.title).maxBy(_.length))
}

object TalkTitleIdsPrinter extends App {

  private val agenda = new ConfituraAgendaReader(Beans.config.conferenceDate, Beans.config.conferenceTimeZone).read()

  println(agenda.talks.map(_.talkId).mkString("\n"))

  agenda.verifyUniqueTalkIds()
}