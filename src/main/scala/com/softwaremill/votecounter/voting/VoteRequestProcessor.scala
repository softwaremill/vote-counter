package com.softwaremill.votecounter.voting

import com.softwaremill.votecounter.db.{Vote, DevicesDao, VotesDao}
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.joda.time.DateTime

case class VoteRequest(voteId: String, deviceKey: String, positive: Boolean, castedAt: DateTime) {

  def toVote(deviceId: Int): Vote = Vote(voteId, deviceId, castedAt, positive)

}

class VoteRequestProcessor(voteDao: VotesDao, devicesDao: DevicesDao) extends LazyLogging {

  def processRequest(request: VoteRequest) {
    devicesDao.findByKey(request.deviceKey) match {
      case Some(device) =>
        val vote = request.toVote(device.deviceId.get)
        if (voteDao.insertIfNew(vote) == vote) {
          logger.info(s"Registered new vote: $vote.")
        } else {
          logger.info(s"Rejected duplicate vote: $vote.")
        }
      case None =>
        logger.warn(s"Received vote request from unidentified device: $request.")
    }
  }

}
