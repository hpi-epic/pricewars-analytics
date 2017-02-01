package de.hpi.epic.pricewars.analytics

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer09, FlinkKafkaProducer09}
import java.util.Properties

import de.hpi.epic.pricewars.logging.{BuyOfferEntrySchema, NewProductEntrySchema}
import de.hpi.epic.pricewars.logging.marketplace.BuyOfferEntry
import de.hpi.epic.pricewars.logging.producer.NewProductEntry
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.joda.time.DateTime

/**
  * Created by Jan on 29.11.2016.
  */
object MerchantStatistics {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    //val properties = ConfigFactory.load
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "vm-mpws2016hp1-05.eaalab.hpi.uni-potsdam.de:9092")
    properties.setProperty("group.id", "merchantStatistics")

    val newProductStream = env.addSource(new FlinkKafkaConsumer09[NewProductEntry]("producer", NewProductEntrySchema, properties))
    val buyOfferStream = env.addSource(new FlinkKafkaConsumer09[BuyOfferEntry]("buyOffer", BuyOfferEntrySchema, properties))


    val expensesStream = newProductStream.map(e => (e.merchant_id, e.amount * e.price * -1)).keyBy(0)
    val earningsStream = buyOfferStream.filter(e => e.http_code == 200).map(e => (e.merchant_id, e.amount * e.price))
    expensesStream.union(earningsStream)
                                      .keyBy(0)
                                      .window(GlobalWindows.create())
                                      .trigger(ContinuousProcessingTimeTrigger.of(Time.minutes(1)))
                                      .reduce((t1, t2) => (t1._1, t1._2 + t2._2))
                                      .map(e => s"""{"merchant_id": "${e._1}", "revenue": ${e._2}, "timestamp": "${new DateTime()}"}""")
                                      .addSink(new FlinkKafkaProducer09("vm-mpws2016hp1-05.eaalab.hpi.uni-potsdam.de:9092",
                                      "revenue", new SimpleStringSchema))

    env.execute()
  }
}
