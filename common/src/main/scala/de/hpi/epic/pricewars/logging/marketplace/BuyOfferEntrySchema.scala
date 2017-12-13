package de.hpi.epic.pricewars.logging.marketplace

import org.apache.flink.streaming.util.serialization.AbstractDeserializationSchema
import org.json4s.Formats
import org.json4s.native.JsonMethods._

import de.hpi.epic.pricewars.logging.MyDateTimeSerializer

/**
  * Created by Jan on 07.02.2017.
  */
object BuyOfferEntrySchema extends AbstractDeserializationSchema[BuyOfferEntry] {
  override def deserialize(message: Array[Byte]): BuyOfferEntry = {
    implicit def formats: Formats = org.json4s.DefaultFormats + MyDateTimeSerializer // ++ org.json4s.ext.JodaTimeSerializers.all

    val json = new String(message)
    parse(json).extract[BuyOfferEntry]
  }
}
