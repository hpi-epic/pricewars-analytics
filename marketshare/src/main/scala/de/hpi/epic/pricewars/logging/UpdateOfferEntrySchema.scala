package de.hpi.epic.pricewars.logging

import de.hpi.epic.pricewars.logging.marketplace.UpdateOfferEntry
import org.apache.flink.streaming.util.serialization.AbstractDeserializationSchema
import org.joda.time.DateTime
import org.json4s.JsonAST.{JNull, JString, JInt}
import org.json4s.{CustomSerializer, Formats}
import org.json4s.native.JsonMethods._

/**
  * Created by Jan on 06.12.2016.
  */

object UpdateOfferEntrySchema extends AbstractDeserializationSchema[UpdateOfferEntry] {
  override def deserialize(message: Array[Byte]): UpdateOfferEntry = {
    implicit def formats: Formats = org.json4s.DefaultFormats + MyDateTimeSerializer

    val json = new String(message)
    parse(json).extract[UpdateOfferEntry]
  }
}

/*case object MyDateTimeSerializer extends CustomSerializer[DateTime](format => (
  {
    case JString(s) => DateTime.parse(s)
    case JInt(i) => new DateTime(i)
    case JNull => null
  },
  {
    case d: DateTime => JString(format.dateFormat.format(d.toDate))
  }
  )
)*/