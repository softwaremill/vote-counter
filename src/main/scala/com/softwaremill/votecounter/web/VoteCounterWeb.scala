package com.softwaremill.votecounter.web

import akka.actor.Actor
import akka.io.IO
import com.softwaremill.votecounter.db.VotesDao
import com.softwaremill.votecounter.infrastructure.Beans
import com.softwaremill.votecounter.voting.{VoteRequestProcessor, VotingResultAggregator}
import com.typesafe.scalalogging.slf4j.StrictLogging
import spray.can.Http
import spray.routing.SimpleRoutingApp

class VoteCounterWebService(beans: Beans) extends Actor with VoteService {
  override implicit def actorRefFactory = context

  override protected val voteDao: VotesDao = beans.voteDao

  override protected val voteRequestProcessor: VoteRequestProcessor = beans.votesRequestProcessor

  override protected val votingResultAggregator: VotingResultAggregator = beans.votingResultAggregator

  override protected val cacheFiles = beans.config.webCacheFiles

  def receive = runRoute(voteServiceRoutes)

}

object VoteCounterWeb extends App with SimpleRoutingApp with StrictLogging {
  val beans = Beans
  implicit val system = beans.actorSystem

  beans.dbInitializer.initializeAndBlock()
  beans.conferenceDataInitializer.initializeAndBlock()

  IO(Http) ! Http.Bind(beans.webHandler, interface = "0.0.0.0", port = 8080)

  beans.sslServer.start()
}
