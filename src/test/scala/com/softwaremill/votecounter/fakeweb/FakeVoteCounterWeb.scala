package com.softwaremill.votecounter.fakeweb

import akka.actor.Props
import akka.io.IO
import com.softwaremill.votecounter.db.Talk
import com.softwaremill.votecounter.infrastructure.Beans
import com.softwaremill.votecounter.voting.{TalkVotingResults, VotingResults}
import com.softwaremill.votecounter.web.{VoteCounterWebService, VoteService}
import com.typesafe.scalalogging.slf4j.StrictLogging
import org.joda.time.DateTime
import spray.can.Http
import spray.http._
import spray.routing.SimpleRoutingApp


trait FakeBeans extends Beans {
  override lazy val webHandler = actorSystem.actorOf(Props(classOf[FakeVoteCounterWebService], this))
}

object FakeBeans extends FakeBeans

trait FakeVoteService extends VoteService {

  def stubsRoute =
    pathPrefix("stubs") {
      getWebappDirectory("stubs")
    }

  def fakeResultsRoute =
    path("results") {
      get {
        complete {
          RealisticVotingResultsSupplier.supply
        }
      }
    } ~
      path("results.csv") {
        get {
          complete {
            HttpResponse(StatusCodes.OK,
              HttpEntity(ContentType(MediaTypes.`text/csv`, HttpCharsets.`UTF-8`),
                resultsToCsvTransformer.asCsv(RealisticVotingResultsSupplier.supply)
              )
            )
          }
        }
      }
}

object RealisticVotingResultsSupplier {

  private val referenceStartTime = new DateTime(2014, 6, 12, 10, 0)

  def supply = VotingResults(Seq(
    TalkVotingResults(Talk("spray_vs_scalatra", "dzem", "Spraył vs. Scalatrą",
      referenceStartTime, referenceStartTime.plusHours(1)), positive = 10, negative = 2, Nil)
  ), Nil, 10, 2
  )
}

class FakeVoteCounterWebService(beans: FakeBeans) extends VoteCounterWebService(beans) with FakeVoteService {
  override def receive = runRoute(voteRoute ~ staticContentRoute ~ fakeResultsRoute ~ stubsRoute)
}

object FakeVoteCounterWeb extends App with SimpleRoutingApp with StrictLogging {
  val beans = FakeBeans
  implicit val system = beans.actorSystem

  beans.dbInitializer.initializeAndBlock()
  beans.conferenceDataInitializer.initializeAndBlock()

  IO(Http) ! Http.Bind(beans.webHandler, interface = "0.0.0.0", port = 8080)
}
