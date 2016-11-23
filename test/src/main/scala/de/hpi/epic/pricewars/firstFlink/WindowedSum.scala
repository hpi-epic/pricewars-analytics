package de.hpi.epic.pricewars.firstFlink

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09
import java.util.Properties
import java.util.concurrent.TimeUnit._

import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.util.serialization.SimpleStringSchema


/**
  * Created by Jan on 22.11.2016.
  */
object WindowedSum {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "vm-mpws2016hp1-05.eaalab.hpi.uni-potsdam.de:9092");
    properties.setProperty("group.id", "test");

    val salesStream = env
      .addSource(new FlinkKafkaConsumer09[SalesEntry]("sales", SalesEntrySchema, properties))

    val mappedStream = salesStream.map(e => (e.offer_id, e.amount))

    val aggStream: KeyedStream[(Long, Long), Tuple] = mappedStream.keyBy(0)

    val windowedSalesStream = aggStream.timeWindowAll(Time.minutes(1), Time.seconds(30)).sum(1)

    windowedSalesStream.print()

    env.execute()
  }
}
