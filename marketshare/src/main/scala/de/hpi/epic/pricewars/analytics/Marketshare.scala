package de.hpi.epic.pricewars.analytics

import com.typesafe.config.ConfigFactory
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer09, FlinkKafkaProducer09}
import de.hpi.epic.pricewars.logging.{BuyOfferEntrySchema, MarketshareEntrySchema, NewProductEntrySchema}
import de.hpi.epic.pricewars.logging.marketplace.{BuyOfferEntry, MarketshareEntry}
import de.hpi.epic.pricewars.config.propsFromConfig
import de.hpi.epic.pricewars.analytics.Algorithms._
import de.hpi.epic.pricewars.logging.flink.MarketshareInputEntry
import de.hpi.epic.pricewars.logging.producer.NewProductEntry

/**
  * Created by Jan on 06.12.2016.
  */
object Marketshare {
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

    setupAmountBased(filteredBuyOfferStream)
    setupTurnoverBased(filteredBuyOfferStream)
    setupRevenueStream(mergedStream)

    env.execute()
  }

  def setupAmountBased(stream: DataStream[BuyOfferEntry]): Unit = {
    val amountStream = kumulativeMarketshare(stream, Time.minutes(1))
    amountStream.addSink(new FlinkKafkaProducer09[MarketshareEntry](
      config.getString("kafka.bootstrap.servers"),
      config.getString("kafka.marketshare.topic.target.amount"),
      MarketshareEntrySchema
    ))

    val amountStreamHourly = kumulativeMarketshare(stream, Time.hours(1))
    amountStreamHourly.addSink(new FlinkKafkaProducer09[MarketshareEntry](
      config.getString("kafka.bootstrap.servers"),
      s"${config.getString("kafka.marketshare.topic.target.amount")}Hourly",
      MarketshareEntrySchema
    ))

    val amountStreamDaily = kumulativeMarketshare(stream, Time.days(1))
    amountStreamDaily.addSink(new FlinkKafkaProducer09[MarketshareEntry](
      config.getString("kafka.bootstrap.servers"),
      s"${config.getString("kafka.marketshare.topic.target.amount")}Daily",
      MarketshareEntrySchema
    ))
  }

  def setupTurnoverBased(stream: DataStream[BuyOfferEntry]) = {
    val turnoverStream = kumulativeMarketshare[BuyOfferEntry](stream, Time.minutes(1),(sale) => {(sale.amount * sale.price).toDouble})
    turnoverStream.addSink(new FlinkKafkaProducer09[MarketshareEntry](
      config.getString("kafka.bootstrap.servers"),
      config.getString("kafka.marketshare.topic.target.turnover"),
      MarketshareEntrySchema))

    val turnoverStreamHourly = kumulativeMarketshare[BuyOfferEntry](stream, Time.hours(1), (sale) => {(sale.amount * sale.price).toDouble})
    turnoverStreamHourly.addSink(new FlinkKafkaProducer09[MarketshareEntry](
      config.getString("kafka.bootstrap.servers"),
      s"${config.getString("kafka.marketshare.topic.target.turnover")}Hourly",
      MarketshareEntrySchema))

    val turnoverStreamDaily = kumulativeMarketshare[BuyOfferEntry](stream, Time.days(1), (sale) => {(sale.amount * sale.price).toDouble})
    turnoverStreamDaily.addSink(new FlinkKafkaProducer09[MarketshareEntry](
      config.getString("kafka.bootstrap.servers"),
      s"${config.getString("kafka.marketshare.topic.target.turnover")}Daily",
      MarketshareEntrySchema))
  }

  def setupRevenueStream(stream: DataStream[MarketshareInputEntry]) = {
    val revenueStream = kumulativeMarketshare(stream, Time.minutes(1))
    revenueStream.addSink(new FlinkKafkaProducer09[MarketshareEntry](
      config.getString("kafka.bootstrap.servers"),
      config.getString("kafka.marketshare.topic.target.revenue"),
      MarketshareEntrySchema))

    val revenueStreamHourly = kumulativeMarketshare(stream, Time.hours(1))
    revenueStreamHourly.addSink(new FlinkKafkaProducer09[MarketshareEntry](
      config.getString("kafka.bootstrap.servers"),
      s"${config.getString("kafka.marketshare.topic.target.revenue")}Hourly",
      MarketshareEntrySchema))

    val revenueStreamDaily = kumulativeMarketshare(stream, Time.days(1))
    revenueStreamDaily.addSink(new FlinkKafkaProducer09[MarketshareEntry](
      config.getString("kafka.bootstrap.servers"),
      s"${config.getString("kafka.marketshare.topic.target.revenue")}Daily",
      MarketshareEntrySchema))
  }
}
