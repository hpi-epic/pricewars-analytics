package de.hpi.epic.pricewars.analytics

import com.typesafe.config.ConfigFactory
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer09, FlinkKafkaProducer09}
import de.hpi.epic.pricewars.logging.{BuyOfferEntrySchema, MarketShareEntrySchema, NewProductEntrySchema}
import de.hpi.epic.pricewars.logging.marketplace.{BuyOfferEntry, MarketshareEntry}
import de.hpi.epic.pricewars.config.propsFromConfig
import de.hpi.epic.pricewars.analytics.Algorithms._
import de.hpi.epic.pricewars.logging.flink.MarketshareInputEntry
import de.hpi.epic.pricewars.logging.producer.NewProductEntry

/**
  * Created by Jan on 06.12.2016.
  */
object MarketShare {
  val config = ConfigFactory.load

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val properties = propsFromConfig(config.getConfig("kafka"))

    val buyOfferStream = env.addSource(
      new FlinkKafkaConsumer09[BuyOfferEntry](
        config.getString("kafka.marketshare.topic.source.buy"),
        BuyOfferEntrySchema,
        properties
      ))

    val producedProductStream = env.addSource(
      new FlinkKafkaConsumer09[NewProductEntry](
        config.getString("kafka.marketshare.topic.source.produce"),
        NewProductEntrySchema,
        properties
      )
    )

    val filteredBuyOfferStream = buyOfferStream.filter(_.http_code == 200)
    val mergedStream = filteredBuyOfferStream.connect(producedProductStream).map(
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
        if (amountCalculation.isDefined) kumulativeMarketshare[T](stream, time, amountCalculation.get)
        else kumulativeMarketshare(stream, time)
      marketShareStream.addSink(new FlinkKafkaProducer09[MarketshareEntry](
        config.getString("kafka.bootstrap.servers"),
        config.getString("kafka.marketshare.topic.target.cumulative." + topic) + extension,
        MarketShareEntrySchema
      ))
    })
  }

  def setupIntervalMarketShare[T <: MarketshareInputT](stream: DataStream[T], topic: String, intervals: Seq[(String, Time)],
                                                       amountCalculation: Option[T => Double] = None): Unit = {
    intervals.foreach(t => {
      val (extension, time) = t
      val marketShareStream =
        if (amountCalculation.isDefined) intervallMarketshare[T](stream, time, amountCalculation.get)
        else intervallMarketshare(stream, time)
      marketShareStream.addSink(new FlinkKafkaProducer09[MarketshareEntry](
        config.getString("kafka.bootstrap.servers"),
        config.getString("kafka.marketshare.topic.target.interval." + topic) + extension,
        MarketShareEntrySchema
      ))
    })
  }
}
