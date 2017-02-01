package de.hpi.epic.pricewars.analytics

import de.hpi.epic.pricewars.logging.base.{MerchantIDEntry, TimestampEntry, ValueEntry}
import de.hpi.epic.pricewars.logging.flink.RevenueEntry
import de.hpi.epic.pricewars.types.{Currency, Token}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.joda.time.DateTime

/**
  * Created by Jan on 01.02.2017.
  */
object RevenueStream {
  type EntryT = MerchantIDEntry with ValueEntry[Currency] with TimestampEntry
  def apply[A](stream: DataStream[A], windowSize: Time, windowSlide: Time)
              (implicit transform: A => RevenueEntry): DataStream[RevenueEntry] = {
    impl(conv(stream)(transform, TypeInformation.of(classOf[RevenueEntry])), windowSize, windowSlide)
  }

  private def conv[A,B](in: DataStream[A])(implicit ev: A => B, ti: TypeInformation[B]): DataStream[B] = {
    //implicit val typeInfo = TypeInformation.of(classOf[RevenueEntry])
    in.map(t => ev(t))
  }

  private def impl(stream: DataStream[RevenueEntry], windowSize: Time, windowSlide: Time): DataStream[RevenueEntry] = {
    implicit val typeInfo = TypeInformation.of(classOf[Token])
    implicit val revenueEntryInfo = TypeInformation.of(classOf[RevenueEntry])
    stream.keyBy(_.merchant_id).window(SlidingProcessingTimeWindows.of(windowSize, windowSlide))
      .reduce((t1, t2) => {
        new RevenueEntry(t1.merchant_id, t1.value + t2.value, t1.timestamp)
      })
      .map(_.copy(timestamp = new DateTime()))
  }
}
