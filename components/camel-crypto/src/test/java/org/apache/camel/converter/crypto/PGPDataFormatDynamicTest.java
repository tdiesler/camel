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
package org.apache.camel.converter.crypto;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class PGPDataFormatDynamicTest extends PGPDataFormatTest {
    // setup a wrong userid
    protected String getKeyUserId() {
        return "wrong";
    }
    // setup a wrong password
    protected String getKeyPassword() {
        return "wrong";
    }
    
    private Map<String, Object> getHeaders() {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(PGPDataFormat.KEY_USERID, "sdude@nowhere.net");
        headers.put(PGPDataFormat.KEY_PASSWORD, "sdude");
        return headers;
    }
    
    @Test
    public void testEncryption() throws Exception {
        doRoundTripEncryptionTests("direct:inline", getHeaders());
    }

    @Test
    public void testEncryption2() throws Exception {
        doRoundTripEncryptionTests("direct:inline2", getHeaders());
    }

    @Test
    public void testEncryptionArmor() throws Exception {
        doRoundTripEncryptionTests("direct:inline-armor", getHeaders());
    }

}
