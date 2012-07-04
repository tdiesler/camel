package ${package};

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;

/**
 * A <a href="http://camel.apache.org/data-format.html">data format</a> ({@link DataFormat})
 * for ${name} data.
 */
public class ${name}DataFormat implements DataFormat {
    public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
        byte[] bytes = exchange.getContext().getTypeConverter().mandatoryConvertTo(byte[].class, graph);
        stream.write(bytes);
    }

    public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
        byte[] bytes = exchange.getContext().getTypeConverter().mandatoryConvertTo(byte[].class, stream);
        return bytes;
    }
}
