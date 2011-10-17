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
package org.apache.camel.component.smpp;

import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.jsmpp.bean.Address;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.MessageClass;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.OptionalParameter;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.ReplaceIfPresentFlag;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.SubmitMultiResult;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.bean.UnsuccessDelivery;
import org.jsmpp.session.SMPPSession;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isNull;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SmppSubmitMultiCommandTest {
    
    private static TimeZone defaultTimeZone;

    private SMPPSession session;
    private SmppConfiguration config;
    private SmppSubmitMultiCommand command;
    
    @BeforeClass
    public static void setUpBeforeClass() {
        defaultTimeZone = TimeZone.getDefault();
        
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
    }
    
    @AfterClass
    public static void tearDownAfterClass() {
        if (defaultTimeZone != null) {
            TimeZone.setDefault(defaultTimeZone);            
        }
    }
    
    @Before
    public void setUp() {
        session = createMock(SMPPSession.class);
        config = new SmppConfiguration();
        
        command = new SmppSubmitMultiCommand(session, config);
    }
    
    @Test
    public void executeWithConfigurationData() throws Exception {
        Exchange exchange = new DefaultExchange(new DefaultCamelContext(), ExchangePattern.InOut);
        exchange.getIn().setHeader(SmppConstants.COMMAND, "SubmitMulti");
        exchange.getIn().setHeader(SmppConstants.ID, "1");
        exchange.getIn().setBody("short message body");
        expect(session.submitMultiple(eq("CMT"), eq(TypeOfNumber.UNKNOWN), eq(NumberingPlanIndicator.UNKNOWN), eq("1616"),
                aryEq(new Address[]{new Address(TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, "1717")}),
                eq(new ESMClass()), eq((byte) 0), eq((byte) 1), (String) isNull(), (String) isNull(), eq(new RegisteredDelivery(SMSCDeliveryReceipt.SUCCESS_FAILURE)),
                eq(ReplaceIfPresentFlag.DEFAULT), eq(new GeneralDataCoding(false, true, MessageClass.CLASS1, Alphabet.ALPHA_DEFAULT)), eq((byte) 0), aryEq("short message body".getBytes()),
                aryEq(new OptionalParameter[0])))
                .andReturn(new SubmitMultiResult("1", new UnsuccessDelivery(new Address(TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, "1717"), 0)));
        
        replay(session);
        
        command.execute(exchange);
        
        verify(session);
        
        assertEquals(Arrays.asList("1"), exchange.getOut().getHeader(SmppConstants.ID));
        assertEquals(1, exchange.getOut().getHeader(SmppConstants.SENT_MESSAGE_COUNT));
        assertNotNull(exchange.getOut().getHeader(SmppConstants.ERROR));
    }
    
    @Test
    public void execute() throws Exception {
        Exchange exchange = new DefaultExchange(new DefaultCamelContext(), ExchangePattern.InOut);
        exchange.getIn().setHeader(SmppConstants.COMMAND, "SubmitMulti");
        exchange.getIn().setHeader(SmppConstants.ID, "1");
        exchange.getIn().setHeader(SmppConstants.SOURCE_ADDR_TON, TypeOfNumber.NATIONAL.value());
        exchange.getIn().setHeader(SmppConstants.SOURCE_ADDR_NPI, NumberingPlanIndicator.NATIONAL.value());
        exchange.getIn().setHeader(SmppConstants.SOURCE_ADDR, "1818");
        exchange.getIn().setHeader(SmppConstants.DEST_ADDR_TON, TypeOfNumber.INTERNATIONAL.value());
        exchange.getIn().setHeader(SmppConstants.DEST_ADDR_NPI, NumberingPlanIndicator.INTERNET.value());
        exchange.getIn().setHeader(SmppConstants.DEST_ADDR, Arrays.asList("1919"));
        exchange.getIn().setHeader(SmppConstants.SCHEDULE_DELIVERY_TIME, new Date(1111111));
        exchange.getIn().setHeader(SmppConstants.VALIDITY_PERIOD, new Date(2222222));
        exchange.getIn().setHeader(SmppConstants.PROTOCOL_ID, (byte) 1);
        exchange.getIn().setHeader(SmppConstants.PRIORITY_FLAG, (byte) 2);
        exchange.getIn().setHeader(SmppConstants.REGISTERED_DELIVERY, new RegisteredDelivery(SMSCDeliveryReceipt.SUCCESS).value());
        exchange.getIn().setHeader(SmppConstants.REPLACE_IF_PRESENT_FLAG, ReplaceIfPresentFlag.REPLACE.value());
        exchange.getIn().setBody("short message body");
        expect(session.submitMultiple(eq("CMT"), eq(TypeOfNumber.NATIONAL), eq(NumberingPlanIndicator.NATIONAL), eq("1818"),
                aryEq(new Address[]{new Address(TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.INTERNET, "1919")}),
                eq(new ESMClass()), eq((byte) 1), eq((byte) 2), eq("-300101001831100-"), eq("-300101003702200-"), eq(new RegisteredDelivery(SMSCDeliveryReceipt.SUCCESS)),
                eq(ReplaceIfPresentFlag.REPLACE), eq(new GeneralDataCoding(false, true, MessageClass.CLASS1, Alphabet.ALPHA_DEFAULT)), eq((byte) 0), aryEq("short message body".getBytes()),
                aryEq(new OptionalParameter[0])))
                .andReturn(new SubmitMultiResult("1"));
        
        replay(session);
        
        command.execute(exchange);
        
        verify(session);
        
        assertEquals(Arrays.asList("1"), exchange.getOut().getHeader(SmppConstants.ID));
        assertEquals(1, exchange.getOut().getHeader(SmppConstants.SENT_MESSAGE_COUNT));
        assertNull(exchange.getOut().getHeader(SmppConstants.ERROR));
    }
}