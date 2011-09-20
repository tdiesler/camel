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
package org.apache.camel.builder.xml;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ErrorListener} which logs the errors.
 */
public class XsltErrorListener implements ErrorListener {

    private static final Logger LOG = LoggerFactory.getLogger(XsltErrorListener.class);

    @Override
    public void warning(TransformerException e) throws TransformerException {
        LOG.warn(e.getMessageAndLocation());
    }

    @Override
    public void error(TransformerException e) throws TransformerException {
        LOG.error(e.getMessageAndLocation(), e);
    }

    @Override
    public void fatalError(TransformerException e) throws TransformerException {
        LOG.error(e.getMessageAndLocation(), e);
    }
}
