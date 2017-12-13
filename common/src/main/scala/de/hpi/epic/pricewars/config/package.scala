package de.hpi.epic.pricewars

import java.util.Properties
import com.typesafe.config.Config

/**
  * Created by Jan on 07.12.2016.
  */
package object config {
  def propsFromConfig(config: Config): Properties = {
    import scala.collection.JavaConversions._

    val props = new Properties()

    val map: Map[String, Object] = config.entrySet().map({ entry =>
      entry.getKey -> entry.getValue.unwrapped()
    })(collection.breakOut)

    props.putAll(map)
    props
  }

  //not thread-safe
  private object UniqueId {
    private var currentId = 0
    def next: Int = {
      currentId += 1
      currentId
    }
  }

  implicit class propsWithClientId(props: Properties) {
    def withClientId(prefix: String): Properties = {
      val id = s"$prefix-${UniqueId.next}"
      val newProps = props.clone().asInstanceOf[Properties]
      newProps.put("client.id", id)
      newProps
    }
  }
}
