package com.softwaremill.votecounter.voting

import org.scalatest.{BeforeAndAfterEach, ShouldMatchers, FlatSpec}
import com.softwaremill.votecounter.testutil.SQLSupport
import com.softwaremill.votecounter.db.{TalksDao, Vote, VotesDao, SmallConference}

class VotingResultAggregatorSpec extends FlatSpec with ShouldMatchers with SQLSupport with SmallConference
with BeforeAndAfterEach {

  private val votesDao = new VotesDao(sqlDatabase)
  private val talksDao = new TalksDao(sqlDatabase)
  private val votingResultsAggregator = new VotingResultAggregator(talksDao, votesDao)

  override protected def beforeEach() = {
    super.beforeEach()
    votesDao.truncate()
  }

  it should "list votes as unclassified if not within vote interval" in {
    val votes = Seq(
      Vote("123", smallRoomDevice.deviceId.get, boringTalk.startsAt.minusHours(1), positive = false),
      Vote("125", smallRoomDevice.deviceId.get, boringTalk.startsAt.minusHours(1).plusMinutes(10), positive = true)
    )

    for (vote <- votes)
      votesDao.insert(vote)


    val results = votingResultsAggregator.aggregateAllVotes

    results.unclassifiedVotesCount shouldEqual 2
    results.totalClassifiedVotes shouldEqual 0
  }

  it should "classify votes for a talk" in {
    val votes = Seq(
      Vote("123", smallRoomDevice.deviceId.get, boringTalk.endsAt.minusMinutes(10), positive = false),
      Vote("125", smallRoomDevice.deviceId.get, boringTalk.endsAt.plusMinutes(10), positive = true),
      Vote("333", largeRoomFirstDevice.deviceId.get, boringTalk.endsAt, positive = true)
    )

    for (vote <- votes)
      votesDao.insert(vote)

    val results = votingResultsAggregator.aggregateAllVotes

    val boringTalkResultsOpt = results.talkVotingResults.find(_.talk.talkId == boringTalk.talkId)

    boringTalkResultsOpt shouldNot be(None)

    boringTalkResultsOpt.get.positive shouldEqual 1
    boringTalkResultsOpt.get.negative shouldEqual 1
  }

  it should "classify votes for a talk from two different devices" in {
    val votes = Seq(
      Vote("123", largeRoomFirstDevice.deviceId.get, interestingTalk.endsAt.minusMinutes(10), positive = true),
      Vote("113", largeRoomFirstDevice.deviceId.get, interestingTalk.endsAt.plusMinutes(10), positive = true),
      Vote("153", largeRoomFirstDevice.deviceId.get, interestingTalk.endsAt.plusMinutes(4), positive = true)
    )

    for (vote <- votes)
      votesDao.insert(vote)

    val results = votingResultsAggregator.aggregateAllVotes

    val interestingTalkResultsOpt = results.talkVotingResults.find(_.talk.talkId == interestingTalk.talkId)

    interestingTalkResultsOpt shouldNot be(None)

    interestingTalkResultsOpt.get.positive shouldEqual 3
    interestingTalkResultsOpt.get.negative shouldEqual 0

    results.positiveVotes shouldEqual 3
    results.negativeVotes shouldEqual 0
    results.totalClassifiedVotes shouldEqual 3
    results.unclassifiedVotesCount shouldEqual 0
  }

}
