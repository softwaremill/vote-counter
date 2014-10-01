package com.softwaremill.votecounter.voting

import com.github.tototoshi.csv._


class ResultsToCsvTransformer(aggregator: VotingResultAggregator) {

  implicit val csvFormat = new DefaultCSVFormat {
    override val delimiter: Char = ','
  }

  def allResultsAsCsv: String = asCsv(aggregator.aggregateAllVotes)

  def asCsv(results: VotingResults): String =
    new LiteCsvWriter(csvFormat).
      toCsvString(results.talkVotingResults.map(tvr => List(tvr.talk.title, tvr.positive, tvr.negative)))

}

class LiteCsvWriter(format: CSVFormat) {

  def toCsvString(seq: Seq[Seq[Any]]): String = {
    seq.map { fields =>
      fields.map(renderField).mkString(format.delimiter.toString)
    }.mkString(format.lineTerminator)
  }

  private def shouldQuote(field: String, quoting: Quoting): Boolean =
    quoting match {
      case QUOTE_ALL => true
      case QUOTE_MINIMAL =>
        List("\r", "\n", format.quoteChar.toString, format.delimiter.toString).exists(field.contains)
      case QUOTE_NONE => false
      case QUOTE_NONNUMERIC =>
        if (field.forall(_.isDigit)) {
          false
        } else {
          val firstCharIsDigit = field.headOption.exists(_.isDigit)
          if (firstCharIsDigit && (field.filterNot(_.isDigit) == ".")) {
            false
          } else {
            true
          }
        }
    }

  private def quote(field: String): String =
    if (shouldQuote(field, format.quoting)) field.mkString(format.quoteChar.toString, "", format.quoteChar.toString)
    else field

  private def repeatQuoteChar(field: String): String =
    field.replace(format.quoteChar.toString, format.quoteChar.toString * 2)

  private def escapeDelimiterChar(field: String): String =
    field.replace(format.delimiter.toString, format.escapeChar.toString + format.delimiter.toString)

  private def show(s: Any): String = s.toString

  private val renderField = {
    val escape = format.quoting match {
      case QUOTE_NONE => escapeDelimiterChar _
      case _ => repeatQuoteChar _
    }
    quote _ compose escape compose show
  }
}