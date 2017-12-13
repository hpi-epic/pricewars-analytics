package de.hpi.epic.pricewars.analytics

import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer09, FlinkKafkaProducer09}
import de.hpi.epic.pricewars.logging.marketplace.{BuyOfferEntry, BuyOfferEntrySchema, MarketShareEntrySchema, MarketshareEntry}
import de.hpi.epic.pricewars.config._
import de.hpi.epic.pricewars.analytics.Algorithms._
import de.hpi.epic.pricewars.logging.flink.MarketshareInputEntry
import de.hpi.epic.pricewars.logging.producer.{Order, OrderSchema}

/**
  * Created by Jan on 06.12.2016.
  */
object MarketShare {
  val config: Config = ConfigFactory.load
  val properties: Properties = propsFromConfig(config.getConfig("kafka.cluster"))
  val clientIdPrefix: String = config.getString("kafka.clientId.prefix")

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val buyOfferStream = env.addSource(
      new FlinkKafkaConsumer09[BuyOfferEntry](
        config.getString("kafka.topic.source.buy"),
        BuyOfferEntrySchema,
        properties withClientId clientIdPrefix
      ))

    val orderStream = env.addSource(
      new FlinkKafkaConsumer09[Order](
        config.getString("kafka.topic.source.produce"),
        OrderSchema,
        properties withClientId clientIdPrefix
      )
    )

    val filteredBuyOfferStream = buyOfferStream.filter(_.http_code == 200)
    val mergedStream = filteredBuyOfferStream.connect(orderStream).map(
      MarketshareInputEntry.from,
      MarketshareInputEntry.from
    )

    val intervals = Seq(
      ("", Time.minutes(1)),
      ("Hourly", Time.hours(1)),
      ("Daily", Time.days(1))
    )

    setupCumulativeMarketShare(filteredBuyOfferStream, "amount", intervals)
    setupCumulativeMarketShare[BuyOfferEntry](filteredBuyOfferStream, "turnover", intervals, Some((e) => (e.amount * e.price).toDouble))
    setupCumulativeMarketShare(mergedStream, "revenue", intervals)

    env.execute("Cumulative aggregation of market share")
  }

  def setupCumulativeMarketShare[T <: MarketshareInputT](stream: DataStream[T], topic: String,
                                                         intervals: Seq[(String, Time)],
                                                         amountCalculation: Option[T => Double] = None): Unit = {
    intervals.foreach(t => {
      val (extension, time) = t
      val marketShareStream =
        if (amountCalculation.isDefined) cumulativeMarketshare[T](stream, time, amountCalculation.get)
        else cumulativeMarketshare(stream, time)
      marketShareStream.addSink(new FlinkKafkaProducer09[MarketshareEntry](
        config.getString("kafka.topic.target.cumulative." + topic) + extension,
        MarketShareEntrySchema,
        properties withClientId clientIdPrefix
      ))
    })
  }
}
