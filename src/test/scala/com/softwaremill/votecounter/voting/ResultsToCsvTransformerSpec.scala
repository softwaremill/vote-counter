package com.softwaremill.votecounter.voting

import com.softwaremill.votecounter.db.Talk
import org.joda.time.DateTime
import org.scalatest.mock.MockitoSugar
import org.scalatest.{ShouldMatchers, FlatSpec}

class ResultsToCsvTransformerSpec extends FlatSpec with ShouldMatchers with MockitoSugar {

  private val referenceStartTime = new DateTime(2014, 6, 12, 10, 0)

  it should "print a correct CSV sequence" in {
    val aggregator = mock[VotingResultAggregator]
    val transformer = new ResultsToCsvTransformer(aggregator)

    val csv = transformer.asCsv(VotingResults(Seq(
      TalkVotingResults(Talk("spray_vs_scalatra", "dzem", "Spraył vs. Scalatrą",
        referenceStartTime, referenceStartTime.plusHours(1)), positive = 10, negative = 2, Nil)
    ), Nil, 10, 2))


    csv shouldEqual "Spraył vs. Scalatrą,10,2"
  }

}
