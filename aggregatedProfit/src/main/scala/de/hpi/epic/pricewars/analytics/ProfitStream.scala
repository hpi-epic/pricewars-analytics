package de.hpi.epic.pricewars.analytics

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.joda.time.DateTime

import de.hpi.epic.pricewars.types.{Currency, Timestamp, Token}
import de.hpi.epic.pricewars.logging.base.{MerchantIDEntry, TimestampEntry, ValueEntry}
import de.hpi.epic.pricewars.logging.flink.{ExpensesEntry, ProfitEntry, RevenueEntry}

/**
  * Created by Jan on 31.01.2017.
  */
object ProfitStream {
  type EntryT = MerchantIDEntry with ValueEntry[Currency] with TimestampEntry
  private implicit val profitEntryTypeInfo = TypeInformation.of(classOf[ProfitEntry])

  /**
    * This function is used to translate an arbitrary stream to a stream consisting of profit entries.
    *
    * In order to transform arbitrary streams into streams of profit entries, a conversion method ev has to be
    * provided implicitly, that can transform values of the input stream into elements, which are subtypes of EntryT.
    * This means, that they have accessors for merchant_id, value (with return type Currency) and timestamp.
    * The class ProfitEntry itself has a transformation function in its companion object to create itself from such
    * an EntryT type.
    *
    * @param in stream that consists of values of type A
    * @param ev implicit function that provides a transformation from type A into type B
    * @tparam A some arbitrary type that the values of stream in have
    * @tparam B a subtype of type EntryT (MerchantIDEntry with ValueEntry[Currency] with TimestampEntry) with accessors
    *           for merchant_id, value with type Currency and timestamp
    * @return returns a stream that contains elements of type ProfitEntry
    */
  private def conv[A,B <: EntryT](in: DataStream[A])(implicit ev: A => B): DataStream[ProfitEntry] = {
    in.map(t => ProfitEntry.from(ev(t)))
  }

  /**
    * Calculation of profit per merchant in a certain time frame with a sliding window.
    *
    * This is actually only a wrapper function for the actual implementation. It handles the conversion of the provided
    * input streams into streams that have the same type, since the union of streams is needed in the implementation
    * to join them
    *
    * @param revenueStream stream that holds values of type A which should represent the earnings of the merchants
    * @param expensesStream stream that holds values of type B which should represent the expenses of the merchants
    * @param windowSize the time span which should be used for the aggregation
    * @param windowSlide the time span after which the sliding window moves
    * @param transformA implicit function that provides a transformation from type A into type RevenueEntry
    * @param transformB implicit function that provides a transformation from type B into type ExpensesEntry
    * @tparam A some arbitrary type that the values of stream revenueStream have. Should represent the earnings.
    * @tparam B some arbitrary type that the values of stream expensesStream have. Should represent the expenses.
    * @return stream that has the aggregated profit (earnings - expenses) per merchant in the specified time span
    */
  def apply[A, B](revenueStream: DataStream[A], expensesStream: DataStream[B], windowSize: Time,
            windowSlide: Time)(implicit transformA: A => RevenueEntry, transformB: B => ExpensesEntry): DataStream[ProfitEntry] = {
    profitImpl(conv(revenueStream), conv(expensesStream), windowSize, windowSlide)
  }

  /**
    * This is the actual implementation of the profit calculation. The expenses and earnings are grouped by merchant,
    * next the window will be applied and finally the difference of all earnings and expenses in the specified time span
    * will be calculated.
    *
    * @param revenueStream stream that consists of the earnings of the merchants
    * @param expensesStream stream that consists of the expenses of the merchants
    * @param windowSize the time span which should be used for the aggregation
    * @param windowSlide the time span after which the sliding window moves
    * @return stream that has the aggregated profit (earnings - expenses) per merchant in the specified time span
    */
  private def profitImpl(revenueStream: DataStream[ProfitEntry], expensesStream: DataStream[ProfitEntry],
                         windowSize: Time, windowSlide: Time): DataStream[ProfitEntry] = {
    implicit val typeInfo = TypeInformation.of(classOf[Token])
    val profitStream =
      revenueStream.union(expensesStream)
        .keyBy(_.merchant_id)

    profitStream.window(SlidingProcessingTimeWindows.of(windowSize, windowSlide))
      .reduce((t1, t2) => {
        new ProfitEntry(t1.merchant_id, t1.value + t2.value, t1.timestamp)
      })
      .map(_.copy(timestamp = new DateTime()))
  }
}
