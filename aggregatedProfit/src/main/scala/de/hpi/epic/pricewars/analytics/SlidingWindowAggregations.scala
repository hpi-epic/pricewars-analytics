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
import de.hpi.epic.pricewars.logging.flink.RevenueEntry
import org.apache.flink.api.java.utils.ParameterTool

/**
  * Created by Jan on 29.11.2016.
  */
object SlidingWindowAggregations {
  val config = ConfigFactory.load

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val properties = propsFromConfig(config.getConfig("kafka"))
    val kafkaUrl = config.getString("kafka.bootstrap.servers")

    val parameter = ParameterTool.fromSystemProperties();
    if (parameter.has("bootstrap.servers")) properties.setProperty("bootstrap.servers", parameter.get("bootstrap.servers"))

    val newProductStream = env.addSource(new FlinkKafkaConsumer09[NewProductEntry](
      config.getString("kafka.topic.source.produce"), NewProductEntrySchema, properties
    )).name("producer stream")
    //filter for successfully executed sales only (http status code is 200)
    val buyOfferStream = env.addSource(new FlinkKafkaConsumer09[BuyOfferEntry](
      config.getString("kafka.topic.source.buy"), BuyOfferEntrySchema, properties
    )).filter(_.http_code == 200).name("marketplace stream")

    //log every 10 seconds profit of last 60 seconds
    ProfitStream(buyOfferStream, newProductStream, Time.minutes(1), Time.seconds(10))
      .addSink(new FlinkKafkaProducer09(kafkaUrl, config.getString("kafka.topic.target.profitPerMinute"), ProfitEntrySchema))
      .name("profit per minute")

    //log every 1 minute profit of the last hour
    ProfitStream(buyOfferStream, newProductStream, Time.hours(1), Time.minutes(1))
      .addSink(new FlinkKafkaProducer09(kafkaUrl, config.getString("kafka.topic.target.profitPerHour"), ProfitEntrySchema))
      .name("profit per hour")

    RevenueStream(buyOfferStream, Time.minutes(1), Time.seconds(10))
      .addSink(new FlinkKafkaProducer09(kafkaUrl, config.getString("kafka.topic.target.revenuePerMinute"), RevenueEntrySchema))
      .name("revenue per minute")

    RevenueStream(buyOfferStream, Time.hours(1), Time.minutes(1))
      .addSink(new FlinkKafkaProducer09(kafkaUrl, config.getString("kafka.topic.target.revenuePerHour"), RevenueEntrySchema))
      .name("revenue per minute")

    env.execute("Sliding Window Profit & Revenue Aggregation")
  }
}
