package de.hpi.epic.pricewars.analytics

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer09, FlinkKafkaProducer09}
import de.hpi.epic.pricewars.config.{propsFromConfig, propsWithClientId}
import de.hpi.epic.pricewars.logging.flink.{ProfitEntrySchema, RevenueEntrySchema}
import de.hpi.epic.pricewars.logging.marketplace.{BuyOfferEntry, BuyOfferEntrySchema}
import de.hpi.epic.pricewars.logging.producer.{Order, OrderSchema}

/**
  * Created by Jan on 29.11.2016.
  */
object SlidingWindowAggregations {
  val config: Config = ConfigFactory.load

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val properties = propsFromConfig(config.getConfig("kafka.cluster"))
    val clientIdPrefix = config.getString("kafka.clientId.prefix")

    val orderStream = env.addSource(new FlinkKafkaConsumer09[Order](
      config.getString("kafka.topic.source.produce"), OrderSchema, properties withClientId clientIdPrefix
    )).name("producer stream")
    //filter for successfully executed sales only (http status code is 200)
    val buyOfferStream = env.addSource(new FlinkKafkaConsumer09[BuyOfferEntry](
      config.getString("kafka.topic.source.buy"), BuyOfferEntrySchema, properties withClientId clientIdPrefix
    )).filter(_.http_code == 200).name("marketplace stream")

    //log every 10 seconds profit [basically: amount * (selling_price - purchase_price)] of last 60 seconds
    ProfitStream(buyOfferStream, orderStream, Time.minutes(1), Time.seconds(10))
      .addSink(new FlinkKafkaProducer09(config.getString("kafka.topic.target.profitPerMinute"),ProfitEntrySchema,properties withClientId clientIdPrefix))
      .name("profit per minute")

    //log every 1 minute profit of the last hour
    ProfitStream(buyOfferStream, orderStream, Time.hours(1), Time.minutes(1))
      .addSink(new FlinkKafkaProducer09(config.getString("kafka.topic.target.profitPerHour"), ProfitEntrySchema, properties withClientId clientIdPrefix))
      .name("profit per hour")

    //log every 10 seconds revenue [basically: amount * selling_price] of last 60 seconds
    RevenueStream(buyOfferStream, Time.minutes(1), Time.seconds(10))
      .addSink(new FlinkKafkaProducer09(config.getString("kafka.topic.target.revenuePerMinute"), RevenueEntrySchema, properties withClientId clientIdPrefix))
      .name("revenue per minute")

    //log every minute revenue of the last hour
    RevenueStream(buyOfferStream, Time.hours(1), Time.minutes(1))
      .addSink(new FlinkKafkaProducer09(config.getString("kafka.topic.target.revenuePerHour"), RevenueEntrySchema, properties withClientId clientIdPrefix))
      .name("revenue per hour")

    env.execute("Sliding Window Profit & Revenue Aggregation")
  }
}
