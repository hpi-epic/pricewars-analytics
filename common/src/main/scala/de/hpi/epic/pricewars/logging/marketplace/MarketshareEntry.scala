package de.hpi.epic.pricewars.logging.marketplace

import de.hpi.epic.pricewars.types._

/**
  * Created by Jan on 07.12.2016.
  */
case class MarketshareEntry(merchant_id: Token, marketshare: Percentage, timestamp: Timestamp) {
  override def toString = s"MarketshareEntry($merchant_id, $marketshare, ${timestamp.getMillis})"
}