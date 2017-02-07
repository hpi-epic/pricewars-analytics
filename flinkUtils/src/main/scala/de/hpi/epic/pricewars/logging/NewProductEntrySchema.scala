package de.hpi.epic.pricewars.logging

import de.hpi.epic.pricewars.logging.producer.NewProductEntry
import org.apache.flink.streaming.util.serialization.AbstractDeserializationSchema
import org.json4s.Formats
import org.json4s.native.JsonMethods._

/**
  * Created by Jan on 30.11.2016.
  */
object NewProductEntrySchema extends AbstractDeserializationSchema[NewProductEntry] {
  override def deserialize(message: Array[Byte]): NewProductEntry = {
    implicit def formats: Formats = org.json4s.DefaultFormats + MyDateTimeSerializer // ++ org.json4s.ext.JodaTimeSerializers.all

    val json = new String(message)
    parse(json).extract[NewProductEntry]
  }
}
