package ${package}

import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultProducer

/**
 * The ${name} producer.
 */
class ${name}Producer(endpoint: ${name}Endpoint) extends DefaultProducer(endpoint) {

  def process(exchange: Exchange) : Unit = {
    println(exchange.getIn().getBody())
  }

}
