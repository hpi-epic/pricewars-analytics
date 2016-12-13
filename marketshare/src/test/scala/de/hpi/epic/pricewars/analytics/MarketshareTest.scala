package de.hpi.epic.pricewars.analytics

import de.hpi.epic.pricewars.logging.MarketshareEntrySchema
import de.hpi.epic.pricewars.logging.marketplace.{BuyOfferEntry, MarketshareEntry}
import org.apache.flink.streaming.api.scala._
import org.scalatest.{FlatSpec, Matchers}
import org.joda.time.DateTime
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.contrib.streaming.DataStreamUtils
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.scalatest.matchers.{MatchResult, Matcher}

import scala.collection.JavaConverters.asScalaIteratorConverter

/**
  * Created by Jan on 13.12.2016.
  */
class MarketshareTest extends FlatSpec with Matchers {
  val data: Seq[BuyOfferEntry] = Seq(
    BuyOfferEntry(1, 1, 1, BigDecimal(12.3), 200, new DateTime(2016, 12, 13, 10, 0)),
    BuyOfferEntry(2, 1, 2, BigDecimal(12.2), 200, new DateTime(2016, 12, 13, 10, 30)),
    BuyOfferEntry(3, 2, 2, BigDecimal(11.0), 200, new DateTime(2016, 12, 13, 10, 50)),
    BuyOfferEntry(2, 1, 1, BigDecimal(11.1), 200, new DateTime(2016, 12, 13, 12, 0)),
    BuyOfferEntry(3, 2, 3, BigDecimal(12.0), 200, new DateTime(2016, 12, 13, 11, 25)),
    BuyOfferEntry(3, 2, 1, BigDecimal(10.0), 200, new DateTime(2016, 12, 13, 11, 45)),
    BuyOfferEntry(1, 1, 2, BigDecimal(12.3), 200, new DateTime(2016, 12, 13, 12, 10))
  )

  "The kumulative marketshare" should "work" in {
    val env = StreamExecutionEnvironment.createLocalEnvironment()
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val stream = env.fromCollection(data)
    val timestampedStream = stream.assignAscendingTimestamps(_.timestamp.getMillis)

    val myResult = Algorithms.kumulativeMarketshare(timestampedStream, Time.hours(1))
    val myOutput: Iterator[MarketshareEntry] = DataStreamUtils.collect(myResult.javaStream).asScala

    new IteratorMatcher(
      Seq(
        MarketshareEntry(1, 3/5.toDouble, new DateTime()),
        MarketshareEntry(2, 2/5.toDouble, new DateTime())
      ),
      (t1: MarketshareEntry, t2: MarketshareEntry) => t1.merchant_id == t2.merchant_id && t1.marketshare == t2.marketshare
    ).matches(myOutput)
  }
}

class IteratorMatcher[T](result: Seq[T], comparator: (T, T) => Boolean) {
  def matches(it: Iterator[T]) = {
    it.zip(result.iterator).foreach(t => {
      assert(comparator(t._1, t._2), s"${t._1} does not match ${t._2}")
    })
  }
}