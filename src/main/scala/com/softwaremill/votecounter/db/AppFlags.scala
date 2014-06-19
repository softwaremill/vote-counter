package com.softwaremill.votecounter.db


class AppFlags(flagDao: FlagDao) {

  import AppFlags._

  def isTestDataInserted = {
    val flagOpt = flagDao.find(TestDataInserted)
    flagOpt match {
      case Some(flag) => flag.value.toBoolean
      case None => false
    }
  }

  def flagTestDataInserted() = {
    flagDao.set(Flag(TestDataInserted, "true"))
  }
}

private[db] object AppFlags {
  val TestDataInserted = "test.data"
}
