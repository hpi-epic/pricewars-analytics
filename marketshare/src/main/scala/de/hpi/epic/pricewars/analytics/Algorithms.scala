package de.hpi.epic.pricewars.analytics

import de.hpi.epic.pricewars.logging.base.{AmountEntry, MerchantIDEntry}
import de.hpi.epic.pricewars.logging.marketplace.{BuyOfferEntry, MarketshareEntry}
import de.hpi.epic.pricewars.types._
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.{GlobalWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger
import org.joda.time.DateTime

/**
  * Created by Jan on 13.12.2016.
  */
object Algorithms {
  type MarketshareInputT = MerchantIDEntry with AmountEntry
  def kumulativeMarketshare[T <: MarketshareInputT](dataStream: DataStream[T], time: Time = Time.minutes(1),
                            amountCalculation: T => Double = (e:T) => e.amount.toDouble): DataStream[MarketshareEntry] = {
    val windowedStream = dataStream.windowAll(GlobalWindows.create())
    val triggeredStream = windowedStream.trigger(ContinuousProcessingTimeTrigger.of(time))
    val aggregatedStream = triggeredStream.fold(Map.empty[ID, Double])((p, c) => {
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

  def intervallMarketshare[T <: MarketshareInputT](dataStream: DataStream[T], time: Time = Time.minutes(1),
                           amountCalculation: T => Double = (e:T) => e.amount.toDouble): DataStream[MarketshareEntry] = {
    val windowedStream = dataStream.timeWindowAll(time)
    val aggregatedStream = windowedStream.fold(Map.empty[ID, Double])((p,c) => {
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
