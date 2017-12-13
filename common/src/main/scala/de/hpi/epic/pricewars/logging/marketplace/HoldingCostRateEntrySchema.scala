package de.hpi.epic.pricewars.logging.marketplace

import org.apache.flink.streaming.util.serialization.AbstractDeserializationSchema
import org.json4s.Formats
import org.json4s.native.JsonMethods._

import de.hpi.epic.pricewars.logging.MyDateTimeSerializer

object HoldingCostRateEntrySchema extends AbstractDeserializationSchema[HoldingCostRateEntry] {
  override def deserialize(message: Array[Byte]): HoldingCostRateEntry = {
    implicit def formats: Formats = org.json4s.DefaultFormats + MyDateTimeSerializer

    val json = new String(message)
    parse(json).extract[HoldingCostRateEntry]
  }
}
