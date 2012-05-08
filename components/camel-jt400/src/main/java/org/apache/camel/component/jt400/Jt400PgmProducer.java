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
package org.apache.camel.component.jt400;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400ByteArray;
import com.ibm.as400.access.AS400DataType;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.component.jt400.Jt400DataQueueEndpoint.Format;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Jt400PgmProducer extends DefaultProducer {

    private static final Logger LOG = LoggerFactory.getLogger(Jt400PgmProducer.class);

    public Jt400PgmProducer(Jt400PgmEndpoint endpoint) {
        super(endpoint);
    }

    private Jt400PgmEndpoint getISeriesEndpoint() {
        return (Jt400PgmEndpoint) super.getEndpoint();
    }

    public void process(Exchange exchange) throws Exception {

        AS400 iSeries = getISeriesEndpoint().getiSeries();

        String commandStr = getISeriesEndpoint().getProgramToExecute();
        ProgramParameter[] parameterList = getParameterList(exchange);

        ProgramCall pgmCall = new ProgramCall(iSeries);
        pgmCall.setProgram(commandStr);
        pgmCall.setParameterList(parameterList);

        if (LOG.isDebugEnabled()) {
            LOG.trace("Starting to call PGM '{}' in host '{}' authentication with the user '{}'",
                    new Object[]{commandStr, iSeries.getSystemName(), iSeries.getUserId()});
        }

        boolean result = pgmCall.run();

        if (LOG.isTraceEnabled()) {
            LOG.trace("Executed PGM '{}' in host '{}'. Success? {}", new Object[]{commandStr, iSeries.getSystemName(), result});
        }

        if (result) {
            handlePGMOutput(exchange, pgmCall, parameterList);
        } else {
            throw new Jt400PgmCallException(getOutputMessages(pgmCall));
        }
    }

    private ProgramParameter[] getParameterList(Exchange exchange) throws InvalidPayloadException, PropertyVetoException {

        Object body = exchange.getIn().getMandatoryBody();

        Object[] params = (Object[]) body;

        ProgramParameter[] parameterList = new ProgramParameter[params.length];
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];

            boolean input;
            boolean output;
            if (getISeriesEndpoint().isFieldIdxForOuput(i)) {
                output = true;
                input = param != null;
            } else {
                output = false;
                input = true;
            }

            byte[] inputData = null;

            // XXX Actually, returns any field length, not just output.
            int length = getISeriesEndpoint().getOutputFieldLength(i);

            if (input) {
                if (param != null) {
                    AS400DataType typeConverter;
                    if (getISeriesEndpoint().getFormat() == Format.binary) {
                        typeConverter = new AS400ByteArray(length);
                    } else {
                        typeConverter = new AS400Text(length, getISeriesEndpoint().getiSeries());
                    }
                    inputData = typeConverter.toBytes(param);
                }
                // Else, inputData will remain null.
            }

            if (input && output) {
                LOG.trace("Parameter {} is both input and output.", i);
                parameterList[i] = new ProgramParameter(inputData, length);
            } else if (input) {
                LOG.trace("Parameter {} is input.", i);
                if (inputData != null) {
                    parameterList[i] = new ProgramParameter(inputData);
                } else {
                    parameterList[i] = new ProgramParameter();
                    parameterList[i].setParameterType(ProgramParameter.PASS_BY_REFERENCE);
                    parameterList[i].setNullParameter(true); // Just for self documentation.
                }
            } else {
                // output
                LOG.trace("Parameter {} is output.", i);
                parameterList[i] = new ProgramParameter(length);
            }
        }

        return parameterList;
    }

    private void handlePGMOutput(Exchange exchange, ProgramCall pgmCall, ProgramParameter[] inputs) throws InvalidPayloadException {

        Object body = exchange.getIn().getMandatoryBody();
        Object[] params = (Object[]) body;

        List<Object> results = new ArrayList<Object>();

        int i = 1;
        for (ProgramParameter pgmParam : pgmCall.getParameterList()) {
            byte[] output = pgmParam.getOutputData();
            Object javaValue = params[i - 1];

            if (output != null) {
                int length = pgmParam.getOutputDataLength();
                AS400DataType typeConverter;
                if (getISeriesEndpoint().getFormat() == Format.binary) {
                    typeConverter = new AS400ByteArray(length);
                } else {
                    typeConverter = new AS400Text(length, getISeriesEndpoint().getiSeries());
                }
                javaValue = typeConverter.toObject(output);
            }

            results.add(javaValue);
            i++;
        }

        Object[] bodyOUT = new Object[results.size()];
        bodyOUT = results.toArray(bodyOUT);

        exchange.getOut().setBody(bodyOUT);
    }

    private String getOutputMessages(ProgramCall pgmCall) throws Exception {
        StringBuilder outputMsg = new StringBuilder();
        // Show messages.
        AS400Message[] messageList = pgmCall.getMessageList();
        for (int i = 0; i < messageList.length; ++i) {
            // Load additional message information.
            messageList[i].load();
            outputMsg.append(i + ") ");
            outputMsg.append(messageList[i].getText());
            outputMsg.append(" - ");
            outputMsg.append(messageList[i].getHelp());
            outputMsg.append("\n");
        }
        return outputMsg.toString();
    }

    @Override
    protected void doStart() throws Exception {
        if (!getISeriesEndpoint().getiSeries().isConnected()) {
            LOG.info("Connecting to {}", getISeriesEndpoint());
            getISeriesEndpoint().getiSeries().connectService(AS400.COMMAND);
        }
    }

    @Override
    protected void doStop() throws Exception {
        if (getISeriesEndpoint().getiSeries().isConnected()) {
            LOG.info("Disconnecting from {}", getISeriesEndpoint());
            getISeriesEndpoint().getiSeries().disconnectAllServices();
        }
    }

}
