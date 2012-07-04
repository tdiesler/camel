package ${package}

import org.apache.camel.Endpoint
import org.apache.camel.impl.DefaultComponent
import java.util.Map

/**
 * Represents the component that manages {@link ${name}Endpoint}.
 */
class ${name}Component extends DefaultComponent {

  protected def createEndpoint(uri: String, remaining: String, parameters: Map[String, Object]): Endpoint = {

    val endpoint = new ${name}Endpoint(uri, this)
    setProperties(endpoint, parameters)

    endpoint
  }
}
