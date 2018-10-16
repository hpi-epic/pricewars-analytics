package de.hpi.epic.pricewars

import java.time.ZonedDateTime

/**
  * Created by Jan on 30.11.2016.
  */
package object types {
  type Amount = Int
  type Quantity = Int
  type Currency = BigDecimal
  type HttpCode = Int
  type ID = Long
  type Token = String
  type Name = String
  type ShippingTime = Int
  type Signature = String
  type Percentage = Double
  type Timestamp = ZonedDateTime
  type Quality = Int
  type PrimeOffer = Boolean
}
