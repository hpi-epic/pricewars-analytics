package de.hpi.epic.pricewars.firstFlink

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer09, FlinkKafkaProducer09}
import java.util.Properties
import java.util.concurrent.TimeUnit._

import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.api.common.functions.{FilterFunction, FlatMapFunction, MapFunction}
import org.apache.flink.api.java.tuple.Tuple2
import org.apache.flink.streaming.api.datastream.DataStream
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector
import org.joda.time.DateTime


/**
  * Created by Jan on 22.11.2016.
  */
object WindowedSum {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val kafkaURL = "vm-mpws2016hp1-05.eaalab.hpi.uni-potsdam.de:9092"

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", kafkaURL);
    properties.setProperty("group.id", "test");

    val salesStream = env
      .addSource(new FlinkKafkaConsumer09[SalesEntry]("buyOffer", SalesEntrySchema, properties))

    val s0 = salesStream.filter(_.http_code == 200)
    val s1 = s0.map(e => (e.offer_id, e.amount))
    val s2 = s1.keyBy(0)
    val s3 = s2.timeWindow(Time.minutes(1), Time.seconds(30))
    val s4 = s3.sum(1)
    val s5 = s4.map(e => s"""{"offer_id": ${e._1}, "amount": ${e._2}, "timestamp": ${new DateTime()}""")
    s5.addSink(new FlinkKafkaProducer09[String](kafkaURL, "SalesPerMinutes", new SimpleStringSchema()))
    s5.print()

    env.execute()
  }
}
