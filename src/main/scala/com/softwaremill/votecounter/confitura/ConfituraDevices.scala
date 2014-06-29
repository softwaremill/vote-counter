package com.softwaremill.votecounter.confitura

import java.text.SimpleDateFormat
import java.util.Locale

import com.softwaremill.votecounter.db.Device
import com.softwaremill.votecounter.util.InputStreams
import org.json4s.DefaultFormats
import org.json4s.ext.JodaTimeSerializers
import org.json4s.jackson.JsonMethods._


trait DevicesProvider {

  def devices: Seq[Device]
}

class ConfituraDevices extends DevicesProvider {

  implicit def json4sJacksonFormats = new DefaultFormats {
    override protected def dateFormatter =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
  } ++ JodaTimeSerializers.all

  override val devices = parseDeviceJson("devices.json")

  private[confitura] def parseDeviceJson(resource: String) =
    parse(InputStreams.inClasspath(resource)).extract[Seq[Device]]
}
