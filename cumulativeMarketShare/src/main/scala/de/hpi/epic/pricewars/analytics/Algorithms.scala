package de.hpi.epic.pricewars.analytics

import de.hpi.epic.pricewars.logging.base.{AmountEntry, MerchantIDEntry}
import de.hpi.epic.pricewars.logging.marketplace.MarketshareEntry
import de.hpi.epic.pricewars.types.Token
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger
import org.joda.time.DateTime


/**
  * Created by Jan on 13.12.2016.
  */
object Algorithms {
  type MarketshareInputT = MerchantIDEntry with AmountEntry
  private implicit val mapTypeInfo = TypeInformation.of(classOf[Map[Token, Double]])
  private implicit val marketshareEntryTypeInfo = TypeInformation.of(classOf[MarketshareEntry])

  def cumulativeMarketshare[T <: MarketshareInputT](dataStream: DataStream[T], time: Time,
                                                    amountCalculation: T => Double = (e: T) => e.amount.toDouble): DataStream[MarketshareEntry] = {
    val windowedStream = dataStream.windowAll(GlobalWindows.create())
    val triggeredStream = windowedStream.trigger(ContinuousProcessingTimeTrigger.of(time))
    val aggregatedStream = triggeredStream.fold(Map.empty[Token, Double])((p, c) => {
      p.get(c.merchant_id) match {
        case Some(value) => p + (c.merchant_id -> (value + amountCalculation(c)))
        case None => p + (c.merchant_id -> amountCalculation(c))
      }
    })
    val expandedStream = aggregatedStream.flatMap(map => {
      val globalSum = map.values.sum
      map.toSeq.map(t => MarketshareEntry(t._1, t._2 / globalSum, new DateTime()))
    })
    expandedStream
  }
}
