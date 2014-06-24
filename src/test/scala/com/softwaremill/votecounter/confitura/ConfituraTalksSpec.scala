package com.softwaremill.votecounter.confitura

import org.scalatest.{ShouldMatchers, FlatSpec}
import com.softwaremill.votecounter.infrastructure.Beans

class ConfituraTalksSpec extends FlatSpec with ShouldMatchers {

  val confituraTalks = new ConfituraTalks(Beans.agendaFileReader,
    Beans.config.conferenceDate, Beans.config.conferenceTimeZone)

  it should "convert all agenda talks" in {
    confituraTalks.talks.length shouldEqual 37
  }
}