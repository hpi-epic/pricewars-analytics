package de.hpi.epic.pricewars.logging.flink

import de.hpi.epic.pricewars.logging.base.{AmountEntry, MerchantIDEntry}
import de.hpi.epic.pricewars.logging.marketplace.BuyOfferEntry
import de.hpi.epic.pricewars.logging.producer.Order
import de.hpi.epic.pricewars.types._
/**
  * Created by Jan on 13.12.2016.
  */
class MarketshareInputEntry(val merchant_id: Token, val amount: Amount) extends AmountEntry with MerchantIDEntry

object MarketshareInputEntry {
  def from(entry: BuyOfferEntry): MarketshareInputEntry = new MarketshareInputEntry(entry.merchant_id, entry.amount)
  def from(order: Order): MarketshareInputEntry = new MarketshareInputEntry(order.merchant_id, order.quantity)
}
