package com.softwaremill.votecounter.confitura

import org.scalatest.{FlatSpec, ShouldMatchers}


class ConfituraDevicesSpec extends FlatSpec with ShouldMatchers {

  it should "successfully parse devices-test.json" in {
    val devices = new ConfituraDevices().parseDeviceJson("devices-test.json")

    devices.length shouldEqual 1
    devices(0).key shouldEqual "123"
  }

}
