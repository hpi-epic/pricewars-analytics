package de.hpi.epic.pricewars.analytics

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.joda.time.DateTime
import de.hpi.epic.pricewars.logging.flink.RevenueEntry
import de.hpi.epic.pricewars.types.Token

object RevenueStream {
  private implicit val revenueEntryTypeInfo: TypeInformation[RevenueEntry] = TypeInformation.of(classOf[RevenueEntry])

  def apply(stream: DataStream[RevenueEntry], windowSize: Time, windowSlide: Time): DataStream[RevenueEntry] = {
    implicit val typeInfo: TypeInformation[Token] = TypeInformation.of(classOf[Token])
    stream.keyBy(_.merchant_id).window(SlidingProcessingTimeWindows.of(windowSize, windowSlide))
      .reduce((t1, t2) => new RevenueEntry(t1.merchant_id, t1.value + t2.value, t1.timestamp))
      .map(_.copy(timestamp = new DateTime()))
  }
}
