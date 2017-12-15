package de.hpi.epic.pricewars.analytics

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer09, FlinkKafkaProducer09}
import com.typesafe.config.ConfigFactory
import de.hpi.epic.pricewars.config._
import de.hpi.epic.pricewars.logging.flink.{ProfitEntry, ProfitEntrySchema}
import de.hpi.epic.pricewars.logging.marketplace.{BuyOfferEntry, BuyOfferEntrySchema}
import de.hpi.epic.pricewars.logging.producer.{Order, OrderSchema}
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger
import org.joda.time.DateTime

/**
  * Created by Jan on 29.11.2016.
  */
object MerchantStatistics {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val config = ConfigFactory.load
    val properties = propsFromConfig(config.getConfig("kafka"))
    val clientIdPrefix = config.getString("kafka.group.id")

    val orderStream = env.addSource(
      new FlinkKafkaConsumer09[Order](
        "producer",
        OrderSchema,
        properties withClientId clientIdPrefix
      ))
    val buyOfferStream = env.addSource(
      new FlinkKafkaConsumer09[BuyOfferEntry](
        "buyOffer",
        BuyOfferEntrySchema,
        properties withClientId clientIdPrefix
      ))

    val expensesStream = orderStream.map(e => ProfitEntry.from(e))
    val earningsStream = buyOfferStream.filter(e => e.http_code == 200).map(e => ProfitEntry.from(e))
    expensesStream.union(earningsStream)
      .keyBy("merchant_id")
      .window(GlobalWindows.create())
      .trigger(ContinuousProcessingTimeTrigger.of(Time.minutes(1)))
      .reduce((t1, t2) => new ProfitEntry(t1.merchant_id, t1.profit + t2.profit, new DateTime()))
      .addSink(new FlinkKafkaProducer09(
        "profit",
        ProfitEntrySchema,
        properties withClientId clientIdPrefix
      ))

    env.execute("Total profit")
  }
}
