package com.softwaremill.votecounter.util

object InputStreams {
  
  def inClasspath(path : String) =
    this.getClass.getClassLoader.getResourceAsStream(path)

}
