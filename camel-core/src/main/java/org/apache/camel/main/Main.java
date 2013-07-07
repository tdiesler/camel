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
package org.apache.camel.main;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBException;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.Registry;
import org.apache.camel.view.ModelFileGenerator;

/**
 * A command line tool for booting up a CamelContext
 *
 * @version 
 */
public class Main extends MainSupport {

    protected static Main instance;
    protected final SimpleRegistry registry = new SimpleRegistry();

    public Main() {
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
        instance = main;
        main.enableHangupSupport();
        main.run(args);
    }

    /**
     * Returns the currently executing main
     *
     * @return the current running instance
     */
    public static Main getInstance() {
        return instance;
    }

    /**
     * Binds the given <code>name</code> to the <code>bean</code> object, so
     * that it can be looked up inside the CamelContext this command line tool
     * runs with.
     * 
     * @param name the used name through which we do bind
     * @param bean the object to bind
     */
    public void bind(String name, Object bean) {
        registry.put(name, bean);
    }

    /**
     * Using the given <code>name</code> does lookup for the bean being already
     * bound using the {@link #bind(String, Object)} method.
     * 
     * @see Registry#lookupByName(String)
     */
    public Object lookup(String name) {
        return registry.get(name);
    }

    /**
     * Using the given <code>name</code> and <code>type</code> does lookup for
     * the bean being already bound using the {@link #bind(String, Object)}
     * method.
     * 
     * @see Registry#lookupByNameAndType(String, Class)
     */
    public <T> T lookup(String name, Class<T> type) {
        return registry.lookupByNameAndType(name, type);
    }

    /**
     * Using the given <code>type</code> does lookup for the bean being already
     * bound using the {@link #bind(String, Object)} method.
     * 
     * @see Registry#findByTypeWithName(Class)
     */
    public <T> Map<String, T> lookupByType(Class<T> type) {
        return registry.findByTypeWithName(type);
    }    
    
    // Implementation methods
    // -------------------------------------------------------------------------

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        postProcessContext();
        getCamelContexts().get(0).start();
    }

    protected void doStop() throws Exception {
        super.doStop();
        if (getCamelContexts().size() > 0) {
            getCamelContexts().get(0).stop();
        }
    }

    protected ProducerTemplate findOrCreateCamelTemplate() {
        return getCamelContexts().get(0).createProducerTemplate();
    }

    protected Map<String, CamelContext> getCamelContextMap() {
        Map<String, CamelContext> answer = new HashMap<String, CamelContext>();

        CamelContext camelContext = createContext();
        if (registry.size() > 0) {
            // set the registry through which we've already bound some beans
            if (DefaultCamelContext.class.isAssignableFrom(camelContext.getClass())) {
                ((DefaultCamelContext) camelContext).setRegistry(registry);
            }
        }

        answer.put("camel-1", camelContext);
        return answer;
    }

    protected CamelContext createContext() {
        return new DefaultCamelContext();
    }

    protected ModelFileGenerator createModelFileGenerator() throws JAXBException {
        return null;
    }
}
