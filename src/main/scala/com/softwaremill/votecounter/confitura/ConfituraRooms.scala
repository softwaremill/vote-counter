package com.softwaremill.votecounter.confitura

import com.softwaremill.votecounter.common.RoomsProvider
import com.softwaremill.votecounter.db.Room

class ConfituraRooms extends RoomsProvider {

  override val rooms = Seq(
    ("Dżem", "dzem"),
    ("Konfitura", "konfitura"),
    ("Marmolada", "marmolada"),
    ("Powidło", "powidlo"),
    ("Spiżarnia (2-gi budynek)", "spizarnia")
  ).map {
    case (name, id) => Room(id, name)
  }
}