package ${package};

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

/**
 * Represents the component that manages {@link ${name}Endpoint}.
 */
public class ${name}Component extends DefaultComponent {

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new ${name}Endpoint(uri, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
