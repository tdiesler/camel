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
package org.apache.camel.component.quickfixj;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.StatefulService;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import quickfix.FixVersions;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.BeginString;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

public class QuickfixjConsumerTest {
    private Exchange mockExchange;
    private Processor mockProcessor;
    private Endpoint mockEndpoint;
    private SessionID sessionID;
    private Message inboundFixMessage;
    
    @Before
    public void setUp() {

        mockExchange = Mockito.mock(Exchange.class);
        org.apache.camel.Message mockCamelMessage = Mockito.mock(org.apache.camel.Message.class);
        Mockito.when(mockExchange.getIn()).thenReturn(mockCamelMessage);
        
        inboundFixMessage = new Message();
        inboundFixMessage.getHeader().setString(BeginString.FIELD, FixVersions.BEGINSTRING_FIX44);
        inboundFixMessage.getHeader().setString(SenderCompID.FIELD, "SENDER");
        inboundFixMessage.getHeader().setString(TargetCompID.FIELD, "TARGET");
        sessionID = MessageUtils.getSessionID(inboundFixMessage);
        Mockito.when(mockCamelMessage.getBody(quickfix.Message.class)).thenReturn(inboundFixMessage);
        
        mockProcessor = Mockito.mock(Processor.class);        
        mockEndpoint = Mockito.mock(Endpoint.class);
        Mockito.when(mockEndpoint.createExchange(ExchangePattern.InOnly)).thenReturn(mockExchange);  
    }
    
    @Test
    public void processExchangeOnlyWhenStarted() throws Exception {
        QuickfixjConsumer consumer = new QuickfixjConsumer(mockEndpoint, mockProcessor);
        
        Assert.assertThat("Consumer should not be automatically started", 
            ((StatefulService)consumer).isStarted(), CoreMatchers.is(false));
        
        consumer.onExchange(mockExchange);
        
        // No expected interaction with processor since component is not started
        Mockito.verifyZeroInteractions(mockProcessor);
        
        consumer.start();
        Assert.assertThat(((StatefulService)consumer).isStarted(), CoreMatchers.is(true));
        
        consumer.onExchange(mockExchange);
        
        // Second message should be processed
        Mockito.verify(mockProcessor).process(Matchers.isA(Exchange.class));
    }
    
    @Test
    public void setExceptionOnExchange() throws Exception {            
        QuickfixjConsumer consumer = new QuickfixjConsumer(mockEndpoint, mockProcessor);
        consumer.start();
        
        Throwable exception = new Exception("Throwable for test");
        Mockito.doThrow(exception).when(mockProcessor).process(mockExchange);
        
        // Simulate a message from the FIX engine
        consumer.onExchange(mockExchange);
        
        Mockito.verify(mockExchange).setException(exception);
    }
    
    @Test
    public void setExceptionOnInOutExchange() throws Exception {            
        org.apache.camel.Message mockCamelOutMessage = Mockito.mock(org.apache.camel.Message.class);
        Mockito.when(mockExchange.getPattern()).thenReturn(ExchangePattern.InOut);
        Mockito.when(mockExchange.hasOut()).thenReturn(true);
        Mockito.when(mockExchange.getOut()).thenReturn(mockCamelOutMessage);
        Mockito.when(mockCamelOutMessage.getBody(Message.class)).thenReturn(new Message());
        
        QuickfixjConsumer consumer = new QuickfixjConsumer(mockEndpoint, mockProcessor);
        consumer.start();
        
        // Simulate a message from the FIX engine
        consumer.onExchange(mockExchange);
        
        Mockito.verify(mockExchange).setException(Mockito.isA(IllegalStateException.class));
    }

    @Test
    public void processInOutExchange() throws Exception {
        org.apache.camel.Message mockCamelOutMessage = Mockito.mock(org.apache.camel.Message.class);
        Mockito.when(mockExchange.hasOut()).thenReturn(true);
        Mockito.when(mockExchange.getOut()).thenReturn(mockCamelOutMessage);
        Message outboundFixMessage = new Message();
        Mockito.when(mockCamelOutMessage.getBody(Message.class)).thenReturn(outboundFixMessage);
        
        QuickfixjConsumer consumer = Mockito.spy(new QuickfixjConsumer(mockEndpoint, mockProcessor));
        Session mockSession = Mockito.spy(TestSupport.createSession(sessionID));
        Mockito.doReturn(mockSession).when(consumer).getSession(MessageUtils.getReverseSessionID(inboundFixMessage));
        Mockito.doReturn(true).when(mockSession).send(Mockito.isA(Message.class));
        
        consumer.start();
        Mockito.when(mockExchange.getPattern()).thenReturn(ExchangePattern.InOut);
        
        consumer.onExchange(mockExchange);
        Mockito.verify(mockExchange, Mockito.never()).setException(Mockito.isA(Exception.class));
        Mockito.verify(mockSession).send(outboundFixMessage);
    }
}
