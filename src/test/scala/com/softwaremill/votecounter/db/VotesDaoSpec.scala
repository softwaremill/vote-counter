package com.softwaremill.votecounter.db

import org.scalatest._
import com.softwaremill.votecounter.testutil.SQLSupport
import com.softwaremill.votecounter.h2.SQLDatabase
import org.joda.time.{DateTimeZone, DateTime}
import scala.Some


trait SmallConference extends BeforeAndAfterAll {

  this: Suite =>

  protected val sqlDatabase: SQLDatabase

  private val roomsDao = new RoomsDao(sqlDatabase)
  private val deviceDao = new DevicesDao(sqlDatabase)

  protected val smallRoom = Room("small", "Small")
  protected val largeRoom = Room("large", "Large")

  protected val smallRoomDevice = Device(Some(1), "123", "small-d", smallRoom.roomId)
  protected val largeRoomFirstDevice = Device(Some(2), "512", "large-d1", largeRoom.roomId)
  protected val largeRoomSecondDevice = Device(Some(2), "768", "large-d2", largeRoom.roomId)

  protected def initSmallConference() {
    for (room <- Seq(smallRoom, largeRoom))
      roomsDao.insert(room)

    for (device <- Seq(smallRoomDevice, largeRoomFirstDevice, largeRoomSecondDevice))
      deviceDao.insert(device)
  }

  override protected def beforeAll() = {
    super.beforeAll()
    initSmallConference()
  }
}

class VotesDaoSpec extends FlatSpec with SQLSupport with ShouldMatchers with SmallConference with BeforeAndAfterEach {

  override protected def beforeEach() = {
    super.beforeEach()
    dao.truncate()
  }

  val dao = new VotesDao(sqlDatabase)

  it should "support inserting a vote only if it's a new one" in {
    val vote = Vote("1234", smallRoomDevice.deviceId.get, DateTime.now().minusDays(1), positive = true)

    val countBeforeAdding = dao.count()
    dao.insertIfNew(vote)
    dao.insertIfNew(vote)
    val countAfterAdding = dao.count()

    (countAfterAdding - countBeforeAdding) shouldEqual 1
  }

  it should "support truncating the table" in {
    val vote = Vote("1234", smallRoomDevice.deviceId.get, DateTime.now().minusDays(1), positive = true)

    dao.insert(vote)
    dao.truncate()
    dao.count() shouldEqual 0
  }

  it should "find votes with their respective room if requested" in {
    val smallRoomVotes = Seq(
      Vote("3141", smallRoomDevice.deviceId.get, DateTime.now().minusDays(1), positive = true),
      Vote("1545", smallRoomDevice.deviceId.get, DateTime.now().minusDays(1).minusMinutes(1), positive = false)
    )

    val largeRoomVotes = Seq(
      Vote("15132", largeRoomFirstDevice.deviceId.get, DateTime.now().minusDays(1).minusMinutes(1), positive = false),
      Vote("14125", largeRoomSecondDevice.deviceId.get, DateTime.now().minusDays(1).minusMinutes(1), positive = false)
    )

    val allVotes = smallRoomVotes ++ largeRoomVotes

    for (vote <- allVotes)
      dao.insert(vote)

    val byRoom = dao.findAllByRoom()

    byRoom(smallRoom).length shouldEqual 2
    byRoom(largeRoom).length shouldEqual 2

    for (vote <- smallRoomVotes)
      byRoom(smallRoom) should contain(vote.copy(castedAt = vote.castedAt.withZone(DateTimeZone.UTC)))

  }


}