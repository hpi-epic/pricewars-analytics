package de.hpi.epic.pricewars.analytics

import com.typesafe.config.ConfigFactory
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer09, FlinkKafkaProducer09}
import de.hpi.epic.pricewars.config._
import de.hpi.epic.pricewars.logging._
import marketplace.BuyOfferEntry
import producer.NewProductEntry
import de.hpi.epic.pricewars.logging.flink.RevenueEntry._
import de.hpi.epic.pricewars.logging.flink.ExpensesEntry._

/**
  * Created by Jan on 29.11.2016.
  */
object SlidingWindowAggregations {
  val config = ConfigFactory.load

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val properties = propsFromConfig(config.getConfig("kafka.cluster"))
    val kafkaUrl = config.getString("kafka.cluster.bootstrap.servers")
    val clientIdPrefix = config.getString("kafka.clientId.prefix")

    val newProductStream = env.addSource(new FlinkKafkaConsumer09[NewProductEntry](
      config.getString("kafka.topic.source.produce"), NewProductEntrySchema, properties withClientId clientIdPrefix
    )).name("producer stream")
    //filter for successfully executed sales only (http status code is 200)
    val buyOfferStream = env.addSource(new FlinkKafkaConsumer09[BuyOfferEntry](
      config.getString("kafka.topic.source.buy"), BuyOfferEntrySchema, properties withClientId clientIdPrefix
    )).filter(_.http_code == 200).name("marketplace stream")

    //log every 10 seconds profit of last 60 seconds
    ProfitStream(buyOfferStream, newProductStream, Time.minutes(1), Time.seconds(10))
      .addSink(new FlinkKafkaProducer09(config.getString("kafka.topic.target.profitPerMinute"),ProfitEntrySchema,properties withClientId clientIdPrefix))
      .name("profit per minute")

    //log every 1 minute profit of the last hour
    ProfitStream(buyOfferStream, newProductStream, Time.hours(1), Time.minutes(1))
      .addSink(new FlinkKafkaProducer09(config.getString("kafka.topic.target.profitPerHour"), ProfitEntrySchema, properties withClientId clientIdPrefix))
      .name("profit per hour")

    RevenueStream(buyOfferStream, Time.minutes(1), Time.seconds(10))
      .addSink(new FlinkKafkaProducer09(config.getString("kafka.topic.target.revenuePerMinute"), RevenueEntrySchema, properties withClientId clientIdPrefix))
      .name("revenue per minute")

    RevenueStream(buyOfferStream, Time.hours(1), Time.minutes(1))
      .addSink(new FlinkKafkaProducer09(config.getString("kafka.topic.target.revenuePerHour"), RevenueEntrySchema, properties withClientId clientIdPrefix))
      .name("revenue per minute")

    env.execute("Sliding Window Profit & Revenue Aggregation")
  }
}
