package com.softwaremill.votecounter.confitura

import com.softwaremill.votecounter.db.Talk
import org.joda.time.{LocalTime, DateTimeZone, LocalDate}

trait TalksProvider {

  def talks: Seq[Talk]
}

class ConfituraTalks(agendaFileReader: AgendaFileReader,
                     confituraDate: LocalDate, confituraTimeZone: DateTimeZone) extends TalksProvider {

  override def talks = {
    val agendaFile = agendaFileReader.read()
    agendaFile.talks.map { td =>
      def withConferenceDate = confituraDate.toDateTime(_ : LocalTime, confituraTimeZone)
      new Talk(td.talkId, td.roomId, td.title, withConferenceDate(td.start), withConferenceDate(td.end),
        None, None)
    }
  }
}
