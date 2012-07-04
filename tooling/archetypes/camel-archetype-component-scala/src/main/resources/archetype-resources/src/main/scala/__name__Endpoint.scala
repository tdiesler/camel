package ${package}

import org.apache.camel.{Consumer, Processor, Producer}
import org.apache.camel.impl.DefaultEndpoint

/**
 * Represents a ${name} endpoint.
 */
class ${name}Endpoint(uri:String, component: ${name}Component) extends DefaultEndpoint(uri, component) {

  def createProducer() : Producer = {
    new ${name}Producer(this)
  }

  def createConsumer(processor: Processor) : Consumer = {
    new ${name}Consumer(this, processor)
  }

  def isSingleton() : Boolean = true

}
