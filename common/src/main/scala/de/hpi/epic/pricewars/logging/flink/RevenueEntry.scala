package de.hpi.epic.pricewars.logging.flink

import de.hpi.epic.pricewars.logging.base._
import de.hpi.epic.pricewars.types._

/**
  * Created by Jan on 31.01.2017.
  */
case class RevenueEntry (merchant_id: Token, revenue: Currency, timestamp: Timestamp) extends MerchantIDEntry
  with ValueEntry[Currency] with TimestampEntry {
  override def value: Currency = revenue
}

object RevenueEntry {
  type EntryT =  MerchantIDEntry with AmountEntry with PriceEntry with TimestampEntry
  implicit def toRevenueEntry(entry: EntryT): RevenueEntry =
    new RevenueEntry(
      entry.merchant_id,
      entry.price * entry.amount,
      entry.timestamp
    )
}
