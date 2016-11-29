package de.hpi.epic.pricewars.firstFlink

import org.apache.flink.streaming.util.serialization.{AbstractDeserializationSchema, DeserializationSchema}

import org.json4s._
import org.json4s.native.JsonMethods._

/**
  * Created by Jan on 29.11.2016.
  */
case class NewOfferEntry(offer_id: Long, uid: Long, product_id: Long, quality: Long, merchant_id: Long,
                         amount: Long, price: BigDecimal, shipping_time_standard: Int, shipping_time_prime: Option[Int],
                         prime: Boolean, signature: String, http_code: Int, timestamp: String)

object NewOfferEntrySchema extends AbstractDeserializationSchema[NewOfferEntry] {
  override def deserialize(message: Array[Byte]): NewOfferEntry = {
    implicit val formats = DefaultFormats

    val json = new String(message)
    parse(json).extract[NewOfferEntry]
  }
}