package de.hpi.epic.pricewars.logging.base

import de.hpi.epic.pricewars.types.Timestamp

/**
  * Created by Jan on 31.01.2017.
  */
trait TimestampEntry {
  def timestamp: Timestamp
}
