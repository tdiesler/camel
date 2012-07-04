package ${package};

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ${name} producer.
 */
public class ${name}Producer extends DefaultProducer {
    private static final transient Logger LOG = LoggerFactory.getLogger(${name}Producer.class);
    private ${name}Endpoint endpoint;

    public ${name}Producer(${name}Endpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public void process(Exchange exchange) throws Exception {
        System.out.println(exchange.getIn().getBody());    
    }

}
