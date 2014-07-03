package com.softwaremill.votecounter.web

import java.text.SimpleDateFormat
import java.util.Locale

import com.softwaremill.votecounter.db.VotesDao
import com.softwaremill.votecounter.voting.{VoteRequest, VoteRequestProcessor, VotingResultAggregator}
import org.json4s.DefaultFormats
import org.json4s.ext.JodaTimeSerializers
import spray.httpx.Json4sJacksonSupport
import spray.routing.HttpService

trait VoteService extends HttpService with Json4sJacksonSupport with WebappPathDirectives {

  implicit def json4sJacksonFormats = new DefaultFormats {
    override protected def dateFormatter =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
  } ++ JodaTimeSerializers.all

  protected val voteDao: VotesDao

  protected val voteRequestProcessor: VoteRequestProcessor

  protected val votingResultAggregator: VotingResultAggregator

  def voteRoute =
    path("votes") {
      get {
        complete {
          voteDao.findAll()
        }
      } ~
        post {
          entity(as[VoteRequest]) { voteRequest =>
            complete {
              voteRequestProcessor.processRequest(voteRequest)
              "OK"
            }
          }
        }
    } ~
      path("results") {
        get {
          complete {
            votingResultAggregator.aggregateAllVotes
          }
        }
      }

  def staticContentRoute = path("charts") {
    getWebappFile("index.html")
  } ~ path("scripts") {
    getWebappDirectory("scripts")
  } ~ path("styles") {
    getWebappDirectory("styles")
  }

}
