package ${package};

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;

/**
 * Represents a ${name} endpoint.
 */
public class ${name}Endpoint extends DefaultEndpoint {

    public ${name}Endpoint() {
    }

    public ${name}Endpoint(String uri, ${name}Component component) {
        super(uri, component);
    }

    public ${name}Endpoint(String endpointUri) {
        super(endpointUri);
    }

    public Producer createProducer() throws Exception {
        return new ${name}Producer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new ${name}Consumer(this, processor);
    }

    public boolean isSingleton() {
        return true;
    }
}
