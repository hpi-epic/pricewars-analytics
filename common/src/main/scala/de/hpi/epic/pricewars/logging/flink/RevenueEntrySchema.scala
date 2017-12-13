package de.hpi.epic.pricewars.logging.flink

import org.apache.flink.streaming.util.serialization.{AbstractDeserializationSchema, SerializationSchema}
import org.json4s.Formats
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.write

import de.hpi.epic.pricewars.logging.MyDateTimeSerializer

/**
  * Created by Jan on 31.01.2017.
  */
object RevenueEntrySchema extends AbstractDeserializationSchema[RevenueEntry] with SerializationSchema[RevenueEntry] {
  override def deserialize(message: Array[Byte]): RevenueEntry = {
    implicit def formats: Formats = org.json4s.DefaultFormats + MyDateTimeSerializer
    val json = new String(message)
    parse(json).extract[RevenueEntry]
  }

    override def serialize(element: RevenueEntry): Array[Byte] = {
      implicit def formats: Formats = org.json4s.DefaultFormats + MyDateTimeSerializer
      val json = write(element)
      json.getBytes
    }
}
