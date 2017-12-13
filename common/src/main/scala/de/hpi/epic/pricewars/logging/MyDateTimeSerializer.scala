package de.hpi.epic.pricewars.logging

import org.joda.time.DateTime
import org.json4s.JsonAST.{JNull, JString, JInt}
import org.json4s.CustomSerializer


case object MyDateTimeSerializer extends CustomSerializer[DateTime](format => ( {
  case JString(s) => DateTime.parse(s)
  case JInt(i) => new DateTime(i)
  case JNull => null
}, {
  case d: DateTime => JString(format.dateFormat.format(d.toDate))
}
)
)
