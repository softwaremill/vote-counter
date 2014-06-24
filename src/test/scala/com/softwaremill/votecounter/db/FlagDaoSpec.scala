package com.softwaremill.votecounter.db

import org.scalatest.{ShouldMatchers, FlatSpec}
import com.softwaremill.votecounter.testutil.SQLSupport


class FlagDaoSpec extends FlatSpec with SQLSupport with ShouldMatchers {

  val flagDao = new FlagsDao(sqlDatabase)

  it should "not find a flag that does not exist in the DB" in {
    flagDao.find("foo.bar") shouldEqual None
  }

  it should "find a flag that's already in the DB" in {
    flagDao.set(Flag("foo.bar", "baz"))

    val foundFlagOpt = flagDao.find("foo.bar")
    foundFlagOpt shouldNot equal(None)

    foundFlagOpt.get.value shouldEqual "baz"
  }

  it should "update a flag that already exists when calling set" in {
    val countBefore = flagDao.count()

    flagDao.set(Flag("test.update", "a"))
    flagDao.set(Flag("test.update", "b"))

    val foundFlagOpt = flagDao.find("test.update")
    foundFlagOpt shouldNot equal(None)
    foundFlagOpt.get.value shouldEqual "b"

    flagDao.count() shouldEqual countBefore + 1
  }

}
