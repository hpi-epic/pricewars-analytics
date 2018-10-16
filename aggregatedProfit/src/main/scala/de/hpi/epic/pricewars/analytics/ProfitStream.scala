package de.hpi.epic.pricewars.analytics

import java.time.ZonedDateTime

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import de.hpi.epic.pricewars.types.Token
import de.hpi.epic.pricewars.logging.flink.ProfitEntry

/**
  * Created by Jan on 31.01.2017.
  */
object ProfitStream {
  private implicit val profitEntryTypeInfo: TypeInformation[ProfitEntry] = TypeInformation.of(classOf[ProfitEntry])

  /**
    * Calculation of profit per merchant in a certain time frame with a sliding window.
    *
    * @param profitStream stream that holds values of type ProfitEntry which should represent the expenses
    *                     and earnings of the merchants
    * @param windowSize the time span which should be used for the aggregation
    * @param windowSlide the time span after which the sliding window moves
    * @return stream that has the aggregated profit (earnings - expenses) per merchant in the specified time span
    */
  def apply(profitStream: DataStream[ProfitEntry], windowSize: Time, windowSlide: Time): DataStream[ProfitEntry] = {
    implicit val typeInfo: TypeInformation[Token] = TypeInformation.of(classOf[Token])
    profitStream
      .keyBy(_.merchant_id)
      .window(SlidingProcessingTimeWindows.of(windowSize, windowSlide))
      .reduce((t1, t2) => new ProfitEntry(t1.merchant_id, t1.value + t2.value, t1.timestamp))
      .map(_.copy(timestamp = ZonedDateTime.now()))
  }
}
