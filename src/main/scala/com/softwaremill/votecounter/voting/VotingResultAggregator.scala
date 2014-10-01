package com.softwaremill.votecounter.voting

import com.softwaremill.votecounter.config.VoteAggregatorConfig
import com.softwaremill.votecounter.db._
import org.joda.time.{DateTime, Interval, Period}

import scala.language.implicitConversions

case class VotingResults(talkVotingResults: Seq[TalkVotingResults], unclassifiedVotes: Seq[Vote],
                         positiveVotes: Int, negativeVotes: Int) {

  def totalClassifiedVotes = positiveVotes + negativeVotes

  def unclassifiedVotesCount = unclassifiedVotes.length

  def merge(that: VotingResults) =
    this.copy(
      this.talkVotingResults ++ that.talkVotingResults,
      this.unclassifiedVotes ++ that.unclassifiedVotes,
      this.positiveVotes + that.positiveVotes,
      this.negativeVotes + that.negativeVotes
    )

}

case class TalkVotingResults(talk: Talk, positive: Int, negative: Int, votes: List[Vote]) {

  def addVote(vote: Vote) = {
    val newPositiveCont = if (vote.positive) positive + 1 else positive
    val newNegativeCount = if (!vote.positive) negative + 1 else negative

    copy(talk, positive = newPositiveCont, negative = newNegativeCount, vote :: votes)
  }
}

object TalkVotingResults {
  def buildForTalk(talk: Talk) = TalkVotingResults(talk, 0, 0, Nil)
}

private[voting] case class IntermediateVotingResults(talkVotingResults: Map[Talk, TalkVotingResults],
                                                     unclassifiedVotes: List[Vote]) {

  def withUnclassifiedVote(vote: Vote) =
    this.copy(unclassifiedVotes = vote :: unclassifiedVotes)

  def withClassifiedVote(vote: Vote, talk: Talk) =
    this.copy(talkVotingResults = talkVotingResults.updated(talk, talkVotingResults(talk).addVote(vote)))

  def toVotingResults = {
    val tvrSeq = talkVotingResults.values.toSeq
    VotingResults(tvrSeq, unclassifiedVotes,
      tvrSeq.foldLeft(0) { case (acc, results) => acc + results.positive},
      tvrSeq.foldLeft(0) { case (acc, results) => acc + results.negative}
    )
  }

}

private[voting] object IntermediateVotingResults {

  val zeros = IntermediateVotingResults(Map().withDefault(talk => TalkVotingResults.buildForTalk(talk)), Nil)

}

class VotingResultAggregator(talksDao: TalksDao, votesDao: VotesDao, config: VoteAggregatorConfig) {

  def aggregateAllVotes : VotingResults =
    aggregateVotesForTalks(votesDao.findAllByRoom(), talksDao.findAllByRoom())

  def aggregateVotesForTalks(votesMap: Map[Room, List[Vote]], talksMap: Map[Room, List[Talk]]) = {
    val votesMapWithDefault = votesMap.withDefaultValue(Nil)
    talksMap.map { case (room, talks) => aggregateVotesInSingleRoom(votesMapWithDefault(room), talks)}
      .reduceLeft { (acc, next) => acc.merge(next)}
  }

  private def aggregateVotesInSingleRoom(votes: List[Vote], talks: List[Talk]): VotingResults = {
    val intervals = talkVoteIntervals(talks)

    votes.foldLeft(IntermediateVotingResults.zeros) { (acc, vote) =>
      findTalkBasedOnIntervalForVote(vote, intervals) match {
        case Some(talk) => acc.withClassifiedVote(vote, talk)
        case None => acc.withUnclassifiedVote(vote)
      }
    }.toVotingResults
  }

  private def findTalkBasedOnIntervalForVote(vote: Vote, intervals: List[(Interval, Talk)]): Option[Talk] =
    intervals find { case (interval, talk) => interval.contains(vote.castedAt)} map (_._2)

  private def talkVoteIntervals(talks: List[Talk]): List[(Interval, Talk)] = {
    for (talk <- talks)
    yield (new Interval(talk.voteStartsAt, talk.voteEndsAt), talk)
  }.sortBy { case (interval, talk) => interval.getStartMillis}

  implicit private def talkWithVoteTimes(talk: Talk): TalkWithVoteTimes = TalkWithVoteTimes(talk, config.sessionsDelay)
}

private[voting] case class TalkWithVoteTimes(talk: Talk, sessionDelay : Period) {

  import com.softwaremill.votecounter.voting.VotingResultAggregator.{VoteWindowEndOffset, VoteWindowStartOffset}

  def voteStartsAt: DateTime = {
    talk.overrideVoteStartsAt match {
      case Some(dateTime) => dateTime
      case None => talk.endsAt.minus(VoteWindowStartOffset).plus(sessionDelay)
    }
  }

  def voteEndsAt: DateTime = {
    talk.overrideVoteEndsAt match {
      case Some(dateTime) => dateTime
      case None => talk.endsAt.plus(VoteWindowEndOffset).plus(sessionDelay)
    }
  }

}


object VotingResultAggregator {
  val VoteWindowStartOffset = Period.minutes(20)
  val VoteWindowEndOffset = Period.minutes(20)
}
