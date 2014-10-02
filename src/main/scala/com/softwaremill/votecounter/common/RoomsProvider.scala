package com.softwaremill.votecounter.common

import com.softwaremill.votecounter.db.Room

trait RoomsProvider {

  def rooms: Seq[Room]
}
