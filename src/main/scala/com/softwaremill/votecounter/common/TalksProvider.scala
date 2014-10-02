package com.softwaremill.votecounter.common

import com.softwaremill.votecounter.db.Talk

trait TalksProvider {

  def talks: Seq[Talk]
}
