package de.hpi.epic.pricewars.utils

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream

/**
  * Created by Jan on 07.02.2017.
  */
package object flink {
  def conv[A,B](in: DataStream[A])(implicit ev: A => B, ti: TypeInformation[B]): DataStream[B] = {
    in.map(t => ev(t))
  }
}
