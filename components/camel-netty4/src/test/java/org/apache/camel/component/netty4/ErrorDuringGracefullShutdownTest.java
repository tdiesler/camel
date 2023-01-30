package org.apache.camel.component.netty4;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.DefaultErrorHandler;
import org.junit.Test;


public class ErrorDuringGracefullShutdownTest extends BaseNettyTest {
    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                // mock server
                from("netty4:tcp://0.0.0.0:{{port}}?textline=true&disconnect=false")
                        .log("Got request ${body}")
                        .setBody(constant("response"));

                from("direct:req")
                        .to("netty4:tcp://127.0.0.1:{{port}}?textline=true");
            }
        };
    }

    @Test
    public void shouldNotTriggerErrorDuringGracefullShutdown() throws Exception {
        // given: successful request
        String response = template.requestBody("direct:req", "test", String.class);
        assertEquals(response,"response");

        // when: context is closed
        context().stop();
        while (context.getStatus() != ServiceStatus.Stopped) {
            Thread.sleep(1);
        }

        // then: there should be no entries in log indicating that the callback was called twice
        assertFalse(LogCaptureAppender.hasEventsFor(DefaultErrorHandler.class));
    }
}
