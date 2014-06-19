package com.softwaremill.votecounter.h2

import com.softwaremill.votecounter.config.VoteCounterConfig
import com.softwaremill.macwire.Macwire
import com.softwaremill.votecounter.infrastructure.Beans


object H2BrowserConsole extends App {
  val config = Beans.config

  new Thread(new Runnable {
    def run() = new org.h2.tools.Console().runTool("-url", SQLDatabase.connectionString(config))
  }).start()

  println("The console is now running in the background.")
}
