package de.hpi.epic.pricewars.analytics

import com.typesafe.config.ConfigFactory
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer09, FlinkKafkaProducer09}
import org.joda.time.DateTime
import de.hpi.epic.pricewars.logging.{BuyOfferEntrySchema, MarketshareEntrySchema}
import de.hpi.epic.pricewars.logging.marketplace.{BuyOfferEntry, MarketshareEntry}
import de.hpi.epic.pricewars.config.propsFromConfig
import de.hpi.epic.pricewars.types._
import org.apache.flink.streaming.util.serialization.SimpleStringSchema

/**
  * Created by Jan on 06.12.2016.
  */
object Marketshare {

  def kumulativeMarketshare(dataStream: DataStream[BuyOfferEntry], time: Time = Time.minutes(1),
                            amountCalculation: BuyOfferEntry => Double = _.amount): DataStream[MarketshareEntry] = {
    val windowedStream = dataStream.filter(e => e.http_code == 200).windowAll(GlobalWindows.create())
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

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val config = ConfigFactory.load
    val properties = propsFromConfig(config.getConfig("kafka"))

    val buyOfferStream = env.addSource(
      new FlinkKafkaConsumer09[BuyOfferEntry](
        config.getString("kafka.marketshare.topic.source"),
        BuyOfferEntrySchema,
        properties
      ))
    val resultStream = kumulativeMarketshare(buyOfferStream, Time.seconds(10),(sale) => {(sale.amount * sale.price).toDouble})

    resultStream.addSink(new FlinkKafkaProducer09[MarketshareEntry](
      config.getString("kafka.bootstrap.serves"),
      config.getString("kafka.marketshare.topic.target"),
      MarketshareEntrySchema))

    env.execute()
  }
}
