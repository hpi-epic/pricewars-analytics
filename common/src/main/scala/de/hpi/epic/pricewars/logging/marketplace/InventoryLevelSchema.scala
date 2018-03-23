package de.hpi.epic.pricewars.logging.marketplace

import de.hpi.epic.pricewars.logging.MyDateTimeSerializer
import org.apache.flink.streaming.util.serialization.{AbstractDeserializationSchema, SerializationSchema}
import org.json4s.Formats
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization._

object InventoryLevelSchema extends AbstractDeserializationSchema[InventoryLevel] with SerializationSchema[InventoryLevel] {
  override def deserialize(message: Array[Byte]): InventoryLevel = {
    implicit def formats: Formats = org.json4s.DefaultFormats + MyDateTimeSerializer
    val json = new String(message)
    parse(json).extract[InventoryLevel]
  }

  override def serialize(element: InventoryLevel): Array[Byte] = {
    implicit def formats: Formats = org.json4s.DefaultFormats + MyDateTimeSerializer
    val json = write(element)
    json.getBytes
  }
}
