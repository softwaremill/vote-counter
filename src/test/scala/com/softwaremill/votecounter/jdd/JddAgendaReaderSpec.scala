package com.softwaremill.votecounter.jdd

import org.scalatest.{ShouldMatchers, FlatSpec}

class JddAgendaReaderSpec extends FlatSpec with ShouldMatchers {

  private val agenda = new JddAgendaReader().read()

  it should "read and parse the jdd/agenda.json file" in {
    agenda.version shouldEqual "2014-10-01 14:35:17.137206+00:00"
  }

  it should "have more than 10 talks (sanity check)" in {
    val numberOfTalks = agenda.lectures.length
    numberOfTalks should be > 10
  }

  it should "produce an agenda without duplicates" in {
    agenda.verifyUniqueTalkIds()
  }
}
