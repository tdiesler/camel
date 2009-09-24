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
package org.apache.camel.example.pojo_messaging;


import java.io.File;

import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CamelContextTest extends CamelSpringTestSupport {
    
    protected int getExpectedRouteCount() {
        return 0;
    }
    
    protected void deleteLockFile() {
        File file = new File("src/data/message1.xml.camelLock");
        file.delete();
        file = new File("src/data/message2.xml.camelLock");
        file.delete();
        file = new File("src/data/message3.xml.camelLock");
        file.delete();
    }
    
    @Before
    public void setUp() throws Exception {
        deleteDirectory("target/messages");
        // delete the lock file
        deleteLockFile();
        super.setUp();
    }
    
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        deleteLockFile();
    }
    
    @Test
    public void testCheckFiles() throws Exception {
        Thread.sleep(5000);
       
        File file = new File("target/messages/emea/hr_pickup");
        file = file.getAbsoluteFile();
        assertTrue("The pickup folder should exists", file.exists());
        assertEquals("There should be 1 dumped files", 1, file.list().length);
        file = new File("target/messages/amer/hr_pickup");
        file = file.getAbsoluteFile();
        assertTrue("The pickup folder should exists", file.exists());
        assertEquals("There should be 2 dumped files", 2, file.list().length);
        
    }

    @Override
    protected AbstractXmlApplicationContext createApplicationContext() {
        
        return new ClassPathXmlApplicationContext(new String[] {"/META-INF/spring/camel-context.xml"});
    }

}
