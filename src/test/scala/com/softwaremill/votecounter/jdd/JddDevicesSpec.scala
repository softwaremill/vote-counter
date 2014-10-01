package com.softwaremill.votecounter.jdd

import org.scalatest.{FlatSpec, ShouldMatchers}


class JddDevicesSpec extends FlatSpec with ShouldMatchers {

  it should "successfully parse devices-test.json" in {
    val devices = new JddDevices().parseDeviceJson("devices-test.json")

    devices.length shouldEqual 1
    devices(0).key shouldEqual "123"
  }
}
