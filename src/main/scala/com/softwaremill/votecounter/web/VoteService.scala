package com.softwaremill.votecounter.web

import java.text.SimpleDateFormat
import java.util.Locale

import com.softwaremill.votecounter.db.VotesDao
import com.softwaremill.votecounter.voting._
import org.json4s.DefaultFormats
import org.json4s.ext.JodaTimeSerializers
import spray.http._
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

  protected val voteCountsAggregator: VoteCountsAggregator

  protected val resultsToCsvTransformer: ResultsToCsvTransformer

  protected def voteRoute =
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
    } ~ pathPrefix("votes") {
      path("counts") {
        get {
          complete {
            voteCountsAggregator.computeCounts()
          }
        }
      }
    } ~ path("results.csv") {
      get {
        complete {
          HttpResponse(StatusCodes.OK,
            HttpEntity(ContentType(MediaTypes.`text/csv`),
              resultsToCsvTransformer.allResultsAsCsv)
          )
        }
      }
    }

  protected def resultsRoute = path("results") {
    get {
      complete {
        votingResultAggregator.aggregateAllVotes
      }
    }
  }

  protected def staticContentRoute = (path("charts") | path("")) {
    getWebappFile("index.html")
  } ~ pathPrefix("scripts") {
    getWebappDirectory("scripts")
  } ~ pathPrefix("styles") {
    getWebappDirectory("styles")
  }

  protected def voteServiceRoutes = voteRoute ~ resultsRoute ~ staticContentRoute

}
