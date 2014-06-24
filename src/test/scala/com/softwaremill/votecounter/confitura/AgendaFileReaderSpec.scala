package com.softwaremill.votecounter.confitura

import org.scalatest.{ShouldMatchers, FlatSpec}
import com.typesafe.scalalogging.slf4j.LazyLogging

class AgendaFileReaderSpec extends FlatSpec with ShouldMatchers with LazyLogging {

  private val agenda = new AgendaFileReader().read()

  it should "read and parse the agenda3.json file" in {

    agenda.version shouldEqual "1.1.27"
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

  private val agenda = new AgendaFileReader().read()

  println(agenda.talks.map(talk => (talk.room, talk.roomId)).distinct.sorted.mkString("\n"))
}

object LongestTalkTitlePrinter extends App {

  private val agenda = new AgendaFileReader().read()

  println(agenda.talks.map(_.title).maxBy(_.length))
}

object TalkTitleIdsPrinter extends App {

  private val agenda = new AgendaFileReader().read()

  println(agenda.talks.map(_.talkId).mkString("\n"))

  agenda.verifyUniqueTalkIds()
}