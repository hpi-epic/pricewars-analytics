package de.hpi.epic.pricewars.logging.marketplace

import de.hpi.epic.pricewars.logging.base.{AmountEntry, MerchantIDEntry, PriceEntry, TimestampEntry}
import de.hpi.epic.pricewars.types._

/**
  * Created by Jan on 30.11.2016.
  */
case class BuyOfferEntry(offer_id: ID, merchant_id: Token, amount: Amount, price: Currency,
                         http_code: HttpCode, timestamp: Timestamp)
  extends java.io.Serializable with MerchantIDEntry with AmountEntry with PriceEntry with TimestampEntry
