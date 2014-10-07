package com.softwaremill.votecounter.confitura

import org.scalatest.{ShouldMatchers, FlatSpec}
import com.softwaremill.votecounter.infrastructure.Beans

class ConfituraTalksSpec extends FlatSpec with ShouldMatchers {

  val confituraTalks = new ConfituraAgendaReader(Beans.config.conferenceDate, Beans.config.conferenceTimeZone)
    .read().talks

  it should "convert all agenda talks" in {
    confituraTalks.length shouldEqual 37
  }
}