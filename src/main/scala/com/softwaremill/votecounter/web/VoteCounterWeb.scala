package com.softwaremill.votecounter.web

import java.text.SimpleDateFormat
import java.util.Locale

import akka.actor.Actor
import akka.io.IO
import com.softwaremill.votecounter.db.VotesDao
import com.softwaremill.votecounter.infrastructure.Beans
import com.softwaremill.votecounter.voting.{VoteRequest, VoteRequestProcessor, VotingResultAggregator}
import com.typesafe.scalalogging.slf4j.StrictLogging
import org.json4s.DefaultFormats
import org.json4s.ext.JodaTimeSerializers
import spray.can.Http
import spray.httpx.Json4sJacksonSupport
import spray.routing.{HttpService, SimpleRoutingApp}

class VoteCounterWebService(beans: Beans) extends Actor with VoteService {
  implicit def actorRefFactory = context

  override protected val voteDao: VotesDao = beans.voteDao

  override protected val voteRequestProcessor: VoteRequestProcessor = beans.votesRequestProcessor

  override protected val votingResultAggregator: VotingResultAggregator = beans.votingResultAggregator

  def receive = runRoute(voteRoute)

}


trait VoteService extends HttpService with Json4sJacksonSupport {

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

}



object VoteCounterWeb extends App with SimpleRoutingApp with StrictLogging {
  val beans = Beans
  implicit val system = beans.actorSystem

  beans.dbInitializer.initializeAndBlock()
  beans.conferenceDataInitializer.initializeAndBlock()

  IO(Http) ! Http.Bind(beans.webHandler, interface = "0.0.0.0", port = 8080)

  beans.sslServer.start()
}
