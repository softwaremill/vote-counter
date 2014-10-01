package com.softwaremill.votecounter.jdd

import org.scalatest.{ShouldMatchers, FlatSpec}

class JddTalksSpec extends FlatSpec with ShouldMatchers {

  val jddTalks = new JddTalks(new JddAgendaReader)

  it should "convert all agenda talks" in {
    jddTalks.talks.length shouldEqual 49
  }
}
