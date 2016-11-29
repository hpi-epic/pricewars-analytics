package de.hpi.epic.pricewars.firstFlink

import java.util.Date

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.util.serialization.{AbstractDeserializationSchema, DeserializationSchema}

import org.json4s._
import org.json4s.native.JsonMethods._

/**
  * Created by Jan on 22.11.2016.
  */
case class SalesEntry(offer_id: Long, amount: Long, price: BigDecimal, http_code: Int, timestamp: String)

object SalesEntrySchema extends AbstractDeserializationSchema[SalesEntry] {
  override def deserialize(message: Array[Byte]): SalesEntry = {
    implicit val formats = DefaultFormats

    val json = new String(message)
    parse(json).extract[SalesEntry]
  }
}
