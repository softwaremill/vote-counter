package com.softwaremill.votecounter.common

import com.softwaremill.votecounter.db.Talk

trait AgendaProvider {

  def read(): Agenda
}

case class Agenda(version: String, talks: Seq[Talk]) {
  def findDuplicateTalksIds = {
    val allTalkIds = talks.map(_.talkId)
    val distinctTalkIds = allTalkIds.distinct

    allTalkIds.diff(distinctTalkIds).distinct
  }

  def verifyUniqueTalkIds() = {
    val duplicateTalksIds = findDuplicateTalksIds
    if (duplicateTalksIds.nonEmpty) {
      throw new IllegalStateException("Found following duplicate talk ids: " +
        s"${duplicateTalksIds.mkString(",")}.")
    }
  }
}
