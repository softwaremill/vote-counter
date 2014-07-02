package com.softwaremill.votecounter.confitura

import org.joda.time.LocalTime
import java.text.{Normalizer, SimpleDateFormat}
import java.util.Locale
import scala.io.Source
import com.softwaremill.votecounter.util.Resources
import org.json4s.{JsonAST, CustomSerializer}
import org.json4s.JsonAST.{JObject, JString}
import java.text.Normalizer.Form


class AgendaFileReader {

  import org.json4s.DefaultFormats
  import org.json4s.ext.JodaTimeSerializers
  import org.json4s.jackson.JsonMethods._

  val AgendaFilePath = "agenda3.json"

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
  )

  private def transformVerTVersion(json: JsonAST.JValue): JsonAST.JValue =
    json.transformField {
      case ("ver", x) => ("version", x)
    }

  private def removeTalksWithoutARoom(json: JsonAST.JValue): JsonAST.JValue = {
    def removeTransformation(): JsonAST.JValue => Boolean = {
      case talk: JObject =>
        talk.findField {
          case ("title", _) => true
          case _ => false
        }.isDefined &&
          talk.findField {
            case ("room", _) => true
            case _ => false
          }.isEmpty
      case _ => false
    }

    json.transformField {
      case ("talks", talks) => ("talks", talks.remove(removeTransformation()))
    }

  }

  private def fixJson(json: JsonAST.JValue): JsonAST.JValue = {
    val transformationChain = transformVerTVersion _ andThen removeTalksWithoutARoom
    transformationChain(json)
  }

  private[confitura] def parseJson(jsonString: String) = {
    val ast = parse(jsonString)
    val fixedAst = fixJson(ast)
    val agenda = fixedAst.extract[Agenda]

    agenda
  }

  def read(): Agenda =
    parseJson(
      Source.fromInputStream(
        Resources.inputStreamInClasspath(AgendaFilePath)
      ).getLines().mkString("\n"))

}

case class Agenda(version: String, talks: Seq[TalkData]) {
  private[confitura] def findDuplicateTalksIds = {
    val allTalkIds = talks.map(_.talkId)
    val distinctTalkIds = allTalkIds.distinct

    allTalkIds.diff(distinctTalkIds).distinct
  }

  private[confitura] def verifyUniqueTalkIds() = {
    val duplicateTalksIds = findDuplicateTalksIds
    if (!duplicateTalksIds.isEmpty) {
      throw new IllegalStateException("Found following duplicate talk ids: " +
        s"${duplicateTalksIds.mkString(",")}.")
    }
  }
}

case class TalkData(title: String, description: String, room: String, start: LocalTime, end: LocalTime,
                    speakers: Seq[Speaker]) {

  private def removeAccents(s: String) =
    Normalizer.normalize(s, Form.NFD)
      .replaceAll("\\p{InCombiningDiacriticalMarks}+", "").replaceAll("ł", "l")

  private def underscore(s: String) =
    removeAccents(s).toLowerCase.replaceAll(" ", "_").replaceAll("[^a-z0-9_]", "")

  def talkId: String = if (title != "TBD") {
    underscore(title)
  } else {
    underscore(s"${roomId}_${start.getHourOfDay}_${start.getMinuteOfHour}")
  }

  def roomId = removeAccents(room.split("\\s").head.toLowerCase)
}

case class Speaker(firstName: String, lastName: String, photo: String)
