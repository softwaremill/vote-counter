package com.softwaremill.votecounter.db

import org.scalatest.{Suite, BeforeAndAfterAll}
import com.softwaremill.votecounter.h2.SQLDatabase
import org.joda.time.DateTime

trait SmallConference extends BeforeAndAfterAll {

  this: Suite =>

  protected val sqlDatabase: SQLDatabase

  private val roomsDao = new RoomsDao(sqlDatabase)
  private val deviceDao = new DevicesDao(sqlDatabase)
  private val talksDao = new TalksDao(sqlDatabase)

  protected val smallRoom = Room("small", "Small")
  protected val largeRoom = Room("large", "Large")

  protected val smallRoomDevice = Device(Some(1), "123", "small-d", smallRoom.roomId)
  protected val largeRoomFirstDevice = Device(Some(2), "512", "large-d1", largeRoom.roomId)
  protected val largeRoomSecondDevice = Device(Some(2), "768", "large-d2", largeRoom.roomId)

  protected val boringTalk = Talk("boring", smallRoom.roomId, "Boring talk", DateTime.now.minusDays(2),
    DateTime.now.minusDays(2).plusHours(1))

  protected val interestingTalk = Talk("Interesting", largeRoom.roomId, "Interesting talk", DateTime.now.minusDays(2).plusHours(1),
    DateTime.now.minusDays(2).plusHours(2))

  protected def initSmallConference() {
    for (room <- Seq(smallRoom, largeRoom))
      roomsDao.insert(room)

    for (device <- Seq(smallRoomDevice, largeRoomFirstDevice, largeRoomSecondDevice))
      deviceDao.insert(device)

    for (talk <- Seq(boringTalk, interestingTalk))
      talksDao.insert(talk)
  }

  override protected def beforeAll() = {
    super.beforeAll()
    initSmallConference()
  }
}