package com.softwaremill.votecounter.util

object Resources {
  
  def inputStreamInClasspath(path : String) =
    this.getClass.getClassLoader.getResourceAsStream(path)
  
  def existsInClasspath(path : String) =
    this.getClass.getClassLoader.getResource(path) != null

}
