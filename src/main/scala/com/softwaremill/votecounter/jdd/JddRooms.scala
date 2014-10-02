package com.softwaremill.votecounter.jdd

import com.softwaremill.votecounter.common.RoomsProvider
import com.softwaremill.votecounter.db.Room

class JddRooms extends RoomsProvider {

  override def rooms = Seq(
    ("Sputnik", "1"),
    ("Helios", "2"),
    ("Messenger", "3"),
    ("Pioneer", "4")
  ).map {
    case (name, id) => Room(id, name)
  }
}
