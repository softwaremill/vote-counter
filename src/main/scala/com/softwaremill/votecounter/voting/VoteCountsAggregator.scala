package com.softwaremill.votecounter.voting


case class CountCategories(unclassified: Counts, classified: Counts, total: Counts)

object CountCategories {
  def computingTotals(unclassified: Counts, classified: Counts) = {
    CountCategories(unclassified, classified, unclassified + classified)
  }
}

case class Counts(total: Int, positive: Int, negative: Int) {
  def +(that: Counts) = {
    Counts(this.total + that.total, this.positive + that.positive, this.negative + that.negative)
  }
}

object Counts {
  def fromPositiveAndNegative(positiveAndNegative: (Int, Int)) =
    Counts(positiveAndNegative._1 + positiveAndNegative._2, positiveAndNegative._1, positiveAndNegative._2)
}


class VoteCountsAggregator(aggregator: VotingResultAggregator) {

  def computeCounts() = {
    val votes = aggregator.aggregateAllVotes
    val unclassified = Counts.fromPositiveAndNegative(votes.unclassifiedVotes.partition(v => v.positive) match {
      case (positive, negative) => (positive.size, negative.size)
    })

    val classified = Counts.fromPositiveAndNegative(
      votes.talkVotingResults.foldLeft((0, 0)) { (acc, tvr) => (acc._1 + tvr.positive, acc._2 + tvr.negative)}
    )

    CountCategories.computingTotals(unclassified, classified)
  }

}
