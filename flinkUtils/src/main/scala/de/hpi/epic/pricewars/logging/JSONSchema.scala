package de.hpi.epic.pricewars.logging

import de.hpi.epic.pricewars.logging.marketplace.BuyOfferEntry
import de.hpi.epic.pricewars.logging.producer.NewProductEntry
import org.apache.flink.streaming.util.serialization.AbstractDeserializationSchema
import org.joda.time.DateTime
import org.json4s.JsonAST.{JNull, JString, JInt}
import org.json4s.{CustomSerializer, DefaultFormats, Formats}
import org.json4s.native.JsonMethods._

import scala.util.Try

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

object BuyOfferEntrySchema extends AbstractDeserializationSchema[BuyOfferEntry] {
  override def deserialize(message: Array[Byte]): BuyOfferEntry = {
    implicit def formats: Formats = org.json4s.DefaultFormats + MyDateTimeSerializer // ++ org.json4s.ext.JodaTimeSerializers.all

    val json = new String(message)
    parse(json).extract[BuyOfferEntry]
  }
}

case object MyDateTimeSerializer extends CustomSerializer[DateTime](format => (
  {
    case JString(s) => DateTime.parse(s)
    case JInt(i) => new DateTime(i)
    case JNull => null
  },
  {
    case d: DateTime => JString(format.dateFormat.format(d.toDate))
  }
  )
)
