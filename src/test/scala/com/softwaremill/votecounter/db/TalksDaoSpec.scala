package com.softwaremill.votecounter.db

import org.scalatest.{ShouldMatchers, FlatSpec}
import com.softwaremill.votecounter.testutil.SQLSupport
import org.joda.time.DateTimeZone

class TalksDaoSpec extends FlatSpec with ShouldMatchers with SQLSupport with SmallConference {

  private val dao = new TalksDao(sqlDatabase)

  it should "support finding talks by room" in {
    val byRoomId = dao.findAllByRoomId()

    byRoomId(smallRoom.roomId).length shouldEqual 1
    byRoomId(largeRoom.roomId).length shouldEqual 1

    val boringTalkWithUTCDateTimes = boringTalk.copy(startsAt = boringTalk.startsAt.withZone(DateTimeZone.UTC),
      endsAt = boringTalk.endsAt.withZone(DateTimeZone.UTC))

    byRoomId(smallRoom.roomId) should contain(boringTalkWithUTCDateTimes)
  }
}