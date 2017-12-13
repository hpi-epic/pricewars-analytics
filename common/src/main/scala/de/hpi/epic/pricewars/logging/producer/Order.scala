package de.hpi.epic.pricewars.logging.producer

import de.hpi.epic.pricewars.logging.base.{AmountEntry, MerchantIDEntry, PriceEntry, TimestampEntry}
import de.hpi.epic.pricewars.types._

/**
  * Created by Jan on 30.11.2016.
  */
case class Order(billing_amount: Currency, uid: ID, product_id: ID, name: Name, quality: Quality, price: Currency, amount: Amount,
                 signature: Signature, merchant_id: Token, timestamp: Timestamp)
  extends MerchantIDEntry with AmountEntry with PriceEntry with TimestampEntry
