package de.hpi.epic.pricewars.logging

import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter

import org.json4s.JsonAST.{JInt, JNull, JString}
import org.json4s.CustomSerializer

case object MyDateTimeSerializer extends CustomSerializer[ZonedDateTime](_ => ( {
  case JString(s) => ZonedDateTime.parse(s)
  case JInt(i) => ZonedDateTime.ofInstant(Instant.ofEpochSecond(i.longValue()), ZoneOffset.UTC)
  case JNull => null
}, {
  case d: ZonedDateTime => JString(d.format(DateTimeFormatter.ISO_INSTANT))
}))
