package com.softwaremill.votecounter.confitura

import java.text.SimpleDateFormat
import java.util.Locale

import com.softwaremill.votecounter.common.DevicesProvider
import com.softwaremill.votecounter.db.Device
import com.softwaremill.votecounter.util.Resources
import org.json4s.DefaultFormats
import org.json4s.ext.JodaTimeSerializers
import org.json4s.jackson.JsonMethods._

class ConfituraDevices extends DevicesProvider {

  implicit def json4sJacksonFormats = new DefaultFormats {
    override protected def dateFormatter =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
  } ++ JodaTimeSerializers.all

  override val devices = parseDeviceJson("confitura/devices.json")

  private[confitura] def parseDeviceJson(resource: String) =
    parse(Resources.inputStreamInClasspath(resource)).extract[Seq[Device]]
}
