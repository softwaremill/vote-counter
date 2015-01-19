package com.softwaremill.votecounter.heartbeat

import com.softwaremill.votecounter.db.DevicesDao
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.joda.time.DateTime

case class HeartbeatRequest(deviceKey: String)

class HeartbeatRequestProcessor(devicesDao: DevicesDao) extends LazyLogging {

  def processRequest(heartbeatRequest: HeartbeatRequest) {
    devicesDao.findByKey(heartbeatRequest.deviceKey) match {
      case Some(device) =>
        devicesDao.updateLastSeen(device.deviceId.get, DateTime.now())
        logger.info(s"Registered heartbeat for device ${heartbeatRequest.deviceKey}")
      case None =>
        logger.warn(s"Received heartbeat from an unregistered device ${heartbeatRequest.deviceKey}")
    }
  }
}
