package com.softwaremill.votecounter.jdd

import com.softwaremill.votecounter.common.TalksProvider
import com.softwaremill.votecounter.db.Talk
import org.joda.time.{DateTimeZone, LocalTime}

class JddTalks(jddAgendaReader: JddAgendaReader) extends TalksProvider {

  lazy val timeZone = DateTimeZone.forID("Europe/Warsaw")

  override def talks: Seq[Talk] = {
    val agenda = jddAgendaReader.read()
    agenda.lectures.map { t =>
      def withConferenceDate = t.date.toDateTime(_: LocalTime, timeZone)
      Talk(t.talkId, t.track, t.lecture, withConferenceDate(t.start), withConferenceDate(t.end))
    }
  }
}
