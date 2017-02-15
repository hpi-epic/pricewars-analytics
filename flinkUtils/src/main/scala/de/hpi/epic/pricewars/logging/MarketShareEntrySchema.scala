package de.hpi.epic.pricewars.logging

import de.hpi.epic.pricewars.logging.marketplace.MarketshareEntry
import org.apache.flink.streaming.util.serialization.{AbstractDeserializationSchema, SerializationSchema}
import org.json4s.Formats
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization._

object MarketShareEntrySchema extends AbstractDeserializationSchema[MarketshareEntry] with SerializationSchema[MarketshareEntry] {
  override def deserialize(message: Array[Byte]): MarketshareEntry = {
    implicit def formats: Formats = org.json4s.DefaultFormats + MyDateTimeSerializer
    val json = new String(message)
    parse(json).extract[MarketshareEntry]
  }

  override def serialize(element: MarketshareEntry): Array[Byte] = {
    implicit def formats: Formats = org.json4s.DefaultFormats + MyDateTimeSerializer
    val json = write(element)
    json.getBytes
  }
}
