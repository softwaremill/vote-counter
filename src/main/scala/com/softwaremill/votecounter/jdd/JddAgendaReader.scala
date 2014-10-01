package com.softwaremill.votecounter.jdd

import java.text.SimpleDateFormat
import java.util.Locale

import com.softwaremill.votecounter.util.Resources
import org.joda.time.{LocalDate, LocalTime}
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JString

import scala.io.Source

class JddAgendaReader {

  import org.json4s.DefaultFormats
  import org.json4s.ext.JodaTimeSerializers
  import org.json4s.jackson.JsonMethods._

  val AgendaFilePath = "jdd/agenda.json"

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

  def read(): JddAgenda = {
    parseJson(
      Source.fromInputStream(
        Resources.inputStreamInClasspath(AgendaFilePath)
      ).getLines().mkString("\n"))
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