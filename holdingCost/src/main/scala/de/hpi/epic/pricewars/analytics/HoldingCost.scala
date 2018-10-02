package de.hpi.epic.pricewars.analytics

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer09, FlinkKafkaProducer09}
import com.typesafe.config.ConfigFactory
import de.hpi.epic.pricewars.config._
import de.hpi.epic.pricewars.logging.flink.{HoldingCostEntry, HoldingCostEntrySchema}
import de.hpi.epic.pricewars.logging.marketplace._
import de.hpi.epic.pricewars.logging.producer.{Order, OrderSchema}
import de.hpi.epic.pricewars.types.{Amount, Currency, Timestamp, Token}
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.co.RichCoMapFunction
import org.joda.time.DateTime
import org.joda.time.Duration

object HoldingCost {
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
    val saleStream = env.addSource(
      new FlinkKafkaConsumer09[BuyOfferEntry](
        "buyOffer",
        BuyOfferEntrySchema,
        properties withClientId clientIdPrefix
      ))

    val fillingStream = orderStream.map(e => InventoryLevel(e.merchant_id, e.quantity, e.timestamp))
    val emptyingStream = saleStream
      .filter(e => e.http_code == 200)
      .map(e => InventoryLevel(e.merchant_id, -1 * e.amount, e.timestamp))

    val inventoryLevelStream = fillingStream.union(emptyingStream)
      .keyBy("merchant_id")
      .reduce((t1, t2) => InventoryLevel(t1.merchant_id, t1.level + t2.level,
        if (t1.timestamp.isAfter(t2.timestamp)) t1.timestamp else t2.timestamp))

    inventoryLevelStream.addSink(new FlinkKafkaProducer09(
      "inventory_level",
      InventoryLevelSchema,
      properties withClientId clientIdPrefix
    ))

    val rates = env.addSource(
      new FlinkKafkaConsumer09[HoldingCostRateEntry](
        "holding_cost_rate",
        HoldingCostRateEntrySchema,
        properties withClientId clientIdPrefix
      ))
      .keyBy("merchant_id")

    inventoryLevelStream.keyBy("merchant_id")
      .connect(rates)
      .map(new HoldingCostFunction)
      .addSink(new FlinkKafkaProducer09(
        "holding_cost",
        HoldingCostEntrySchema,
        properties withClientId clientIdPrefix
      ))

    env.execute("Holding cost")
  }
}

class HoldingCostFunction extends RichCoMapFunction[InventoryLevel, HoldingCostRateEntry, HoldingCostEntry] {
  private var holding_cost_rate: ValueState[Currency] = _
  private var last_timestamp: ValueState[DateTime] = _
  private var last_inventory_level: ValueState[Amount] = _

  def holding_cost(merchant_id: Token, timestamp: DateTime) : HoldingCostEntry = {
    var cost: Currency = 0
    if (last_timestamp.value() != null) {
      val duration = new Duration(last_timestamp.value(), timestamp)
      cost = last_inventory_level.value() * holding_cost_rate.value() * (duration.getMillis / (60 * 1000.0))
    }
    last_timestamp.update(timestamp)
    HoldingCostEntry(merchant_id, cost, timestamp)
  }

  override def map1(value: InventoryLevel) : HoldingCostEntry = {
    val cost = holding_cost(value.merchant_id, value.timestamp)
    last_inventory_level.update(value.level)
    cost
  }

  override def map2(value: HoldingCostRateEntry) : HoldingCostEntry = {
    val cost = holding_cost(value.merchant_id, value.timestamp)
    holding_cost_rate.update(value.rate)
    cost
  }

  override def open(parameters: Configuration): Unit = {
    holding_cost_rate = getRuntimeContext.getState(
      new ValueStateDescriptor[Currency]("holding cost rate", createTypeInformation[Currency], 0)
    )
    last_timestamp = getRuntimeContext.getState(
      new ValueStateDescriptor[DateTime]("last timestamp", createTypeInformation[DateTime], null)
    )
    last_inventory_level = getRuntimeContext.getState(
      new ValueStateDescriptor[Amount]("last inventory level", createTypeInformation[Amount], 0)
    )
  }
}
