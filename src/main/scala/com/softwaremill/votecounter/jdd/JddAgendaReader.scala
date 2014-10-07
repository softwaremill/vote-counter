package com.softwaremill.votecounter.jdd

import java.text.SimpleDateFormat
import java.util.Locale

import com.softwaremill.votecounter.common.{Agenda, AgendaProvider}
import com.softwaremill.votecounter.db.Talk
import com.softwaremill.votecounter.util.Resources
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.joda.time.{DateTimeZone, LocalDate, LocalTime}
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JString

import scala.io.Source

class JddAgendaReader extends AgendaProvider with LazyLogging {

  import org.json4s.DefaultFormats
  import org.json4s.ext.JodaTimeSerializers
  import org.json4s.jackson.JsonMethods._

  val AgendaFilePath = "jdd/agenda.json"
  lazy val TimeZone = DateTimeZone.forID("Europe/Warsaw")

  implicit val formats = new DefaultFormats {
    override protected def dateFormatter =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
  } ++ JodaTimeSerializers.all + new CustomSerializer[LocalTime](format => ( {
    case JString(time) =>
      val timeVals = time.split(":").map(_.toInt)
      new LocalTime(timeVals(0), timeVals(1))
  }, {
    case x: LocalTime =>
      JString(s"${x.hourOfDay()}:${x.minuteOfHour()}")
  })
  ) + new CustomSerializer[LocalDate](format => ( {
    case JString(date) =>
      val dateVals = date.split("-").map(_.toInt)
      new LocalDate(dateVals(0), dateVals(1), dateVals(2))
  }, {
    case x: LocalDate =>
      JString(s"${x.year()}-${x.monthOfYear()}-${x.dayOfMonth()}")
  })
  )

  private def parseJson(jsonString: String) = {
    parse(jsonString).extract[JddAgenda]
  }

  protected def json: String = {
    logger.info(s"Retrieving JDD agenda from file: $AgendaFilePath")
    Source.fromInputStream(
      Resources.inputStreamInClasspath(AgendaFilePath)
    ).getLines().mkString("\n")
  }

  def read(): Agenda = {
    val agenda = parseJson(json)

    val talks = agenda.lectures.map { t =>
      def withConferenceDate = t.date.toDateTime(_: LocalTime, TimeZone)
      Talk(t.talkId, t.track, t.lecture, withConferenceDate(t.start), withConferenceDate(t.end))
    }

    Agenda(agenda.version, talks)
  }
}

case class JddAgenda(version: String, lectures: Seq[JddTalkData]) {

  private[jdd] def findDuplicateTalksIds = {
    val allTalkIds = lectures.map(_.talkId)
    val distinctTalkIds = allTalkIds.distinct

    allTalkIds.diff(distinctTalkIds).distinct
  }

  private[jdd] def verifyUniqueTalkIds() = {
    val duplicateTalksIds = findDuplicateTalksIds
    if (duplicateTalksIds.nonEmpty) {
      throw new IllegalStateException("Found following duplicate talk ids: " +
        s"${duplicateTalksIds.mkString(",")}.")
    }
  }
}

case class JddTalkData(track: String, lecture: String, date: LocalDate, start: LocalTime, end: LocalTime) {

  def talkId: String = s"${track}_${date}_${start.getHourOfDay}_${start.getMinuteOfHour}"
}