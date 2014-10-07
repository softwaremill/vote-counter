package com.softwaremill.votecounter.jdd

import org.scalatest.{ShouldMatchers, FlatSpec}

class JddTalksSpec extends FlatSpec with ShouldMatchers {

  val jddTalks = new JddAgendaReader().read().talks

  it should "convert all agenda talks" in {
    jddTalks.length shouldEqual 49
  }
}
