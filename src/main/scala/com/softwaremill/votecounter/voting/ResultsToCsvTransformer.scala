package com.softwaremill.votecounter.voting

import java.io.StringWriter

import com.github.tototoshi.csv.CSVWriter


class ResultsToCsvTransformer(aggregator: VotingResultAggregator) {

  implicit val csvFormat = com.github.tototoshi.csv.defaultCSVFormat

  def allResultsAsCsv: String = asCsv(aggregator.aggregateAllVotes)

  protected def asCsv(results: VotingResults): String = {
    val stringWriter = new StringWriter()
    CSVWriter.open(stringWriter).
      writeAll(results.talkVotingResults.map(tvr => List(tvr.talk.title, tvr.positive, tvr.negative)))

    stringWriter.close()
    stringWriter.toString
  }

}
