/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.netty4;

import java.util.Date;
import java.util.Properties;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.serialization.ClassResolvers;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.netty4.codec.ObjectDecoder;
import org.apache.camel.component.netty4.codec.ObjectEncoder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.AvailablePortFinder;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Object Serialization is not allowed by default. However it can be enabled by adding specific encoders/decoders.
 */
public class ObjectSerializationTest extends BaseNettyTest {

    private static volatile int port2;

    @BeforeClass
    public static void initPort2() throws Exception {
        port2 = AvailablePortFinder.getNextAvailable();
    }

    @Test
    public void testObjectSerializationFailureByDefault() throws Exception {
        Date date = new Date();
        try {
            template.requestBody("netty4:tcp://localhost:{{port}}?sync=true&encoders=#encoder", date, Date.class);
            fail("Should have thrown exception");
        } catch (CamelExecutionException e) {
            // expected
        }
    }

    @Test
    public void testObjectSerializationAllowedViaDecoder() throws Exception {
        Date date = new Date();
        Date receivedDate = template.requestBody("netty4:tcp://localhost:{{port2}}?sync=true&encoders=#encoder&decoders=#decoder", date, Date.class);
        assertEquals(date, receivedDate);
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();

        jndi.lookup("prop", Properties.class).setProperty("port2", "" + port2);
        jndi.bind("encoder", new ShareableChannelHandlerFactory(new ObjectEncoder()));
        jndi.bind("decoder", new DefaultChannelHandlerFactory() {
            @Override
            public ChannelHandler newChannelHandler() {
                return new ObjectDecoder(ClassResolvers.weakCachingResolver(null));
            }
        });

        return jndi;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("netty4:tcp://localhost:{{port}}?sync=true")
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            Object obj = exchange.getIn().getBody();
                            exchange.getOut().setBody(obj);
                        }
                    });

                from("netty4:tcp://localhost:{{port2}}?sync=true&decoders=#decoder&encoders=#encoder")
                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            Object obj = exchange.getIn().getBody();
                            exchange.getOut().setBody(obj);
                        }
                    });
            }
        };
    }

}
