package com.softwaremill.votecounter.common

import com.softwaremill.votecounter.db.Device

trait DevicesProvider {

  def devices: Seq[Device]
}
