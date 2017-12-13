package de.hpi.epic.pricewars.logging.flink

import org.apache.flink.streaming.util.serialization.{AbstractDeserializationSchema, SerializationSchema}
import org.json4s.Formats
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.write

import de.hpi.epic.pricewars.logging.MyDateTimeSerializer

object HoldingCostEntrySchema extends AbstractDeserializationSchema[HoldingCostEntry] with SerializationSchema[HoldingCostEntry] {
  override def deserialize(message: Array[Byte]): HoldingCostEntry = {
    implicit def formats: Formats = org.json4s.DefaultFormats + MyDateTimeSerializer
    val json = new String(message)
    parse(json).extract[HoldingCostEntry]
  }

    override def serialize(element: HoldingCostEntry): Array[Byte] = {
      implicit def formats: Formats = org.json4s.DefaultFormats + MyDateTimeSerializer
      val json = write(element)
      json.getBytes
    }
}
