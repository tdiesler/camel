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
package org.apache.camel.util;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.util.jndi.ExampleBean;

/**
 * Unit test for IntrospectionSupport 
 */
public class IntrospectionSupportTest extends ContextTestSupport {

    public void testOverloadSetterChooseStringSetter() throws Exception {
        MyOverloadedBean overloadedBean = new MyOverloadedBean();
        IntrospectionSupport.setProperty(context.getTypeConverter(), overloadedBean, "bean", "James");
        assertEquals("James", overloadedBean.getName());
    }

    public void testOverloadSetterChooseBeanSetter() throws Exception {
        MyOverloadedBean overloadedBean = new MyOverloadedBean();
        ExampleBean bean = new ExampleBean();
        bean.setName("Claus");
        IntrospectionSupport.setProperty(context.getTypeConverter(), overloadedBean, "bean", bean);
        assertEquals("Claus", overloadedBean.getName());
    }

    public void testOverloadSetterChooseUsingTypeConverter() throws Exception {
        MyOverloadedBean overloadedBean = new MyOverloadedBean();
        Object value = "Willem".getBytes();
        // should use byte[] -> String type converter and call the setBean(String) setter method 
        IntrospectionSupport.setProperty(context.getTypeConverter(), overloadedBean, "bean", value);
        assertEquals("Willem", overloadedBean.getName());
    }

    public class MyOverloadedBean {
        private ExampleBean bean;

        public void setBean(ExampleBean bean) {
            this.bean = bean;
        }

        public void setBean(String name) {
            bean = new ExampleBean();
            bean.setName(name);
        }

        public String getName() {
            return bean.getName();
        }
    }

    public void testHasProperties() throws Exception {
        Map<String, Object> empty = CastUtils.cast(Collections.emptyMap());
        assertFalse(IntrospectionSupport.hasProperties(empty, null));
        assertFalse(IntrospectionSupport.hasProperties(empty, ""));
        assertFalse(IntrospectionSupport.hasProperties(empty, "foo."));

        Map<String, Object> param = new HashMap<String, Object>();
        assertFalse(IntrospectionSupport.hasProperties(param, null));
        assertFalse(IntrospectionSupport.hasProperties(param, ""));
        assertFalse(IntrospectionSupport.hasProperties(param, "foo."));

        param.put("name", "Claus");
        assertTrue(IntrospectionSupport.hasProperties(param, null));
        assertTrue(IntrospectionSupport.hasProperties(param, ""));
        assertFalse(IntrospectionSupport.hasProperties(param, "foo."));

        param.put("foo.name", "Hadrian");
        assertTrue(IntrospectionSupport.hasProperties(param, null));
        assertTrue(IntrospectionSupport.hasProperties(param, ""));
        assertTrue(IntrospectionSupport.hasProperties(param, "foo."));
    }

    public void testGetProperties() throws Exception {
        ExampleBean bean = new ExampleBean();
        bean.setName("Claus");
        bean.setPrice(10.0);

        Map<String, Object> map = new HashMap<String, Object>();
        IntrospectionSupport.getProperties(bean, map, null);
        assertEquals(2, map.size());

        assertEquals("Claus", map.get("name"));
        String price = map.get("price").toString();
        assertTrue(price.startsWith("10"));
    }

    public void testGetPropertiesOptionPrefix() throws Exception {
        ExampleBean bean = new ExampleBean();
        bean.setName("Claus");
        bean.setPrice(10.0);

        Map<String, Object> map = new HashMap<String, Object>();
        IntrospectionSupport.getProperties(bean, map, "bean.");
        assertEquals(2, map.size());

        assertEquals("Claus", map.get("bean.name"));
        String price = map.get("bean.price").toString();
        assertTrue(price.startsWith("10"));
    }

    public void testGetProperty() throws Exception {
        ExampleBean bean = new ExampleBean();
        bean.setName("Claus");
        bean.setPrice(10.0);

        Object name = IntrospectionSupport.getProperty(bean, "name");
        assertEquals("Claus", name);
    }

    public void testGetPropertyGetter() throws Exception {
        ExampleBean bean = new ExampleBean();
        bean.setName("Claus");
        bean.setPrice(10.0);

        Method name = IntrospectionSupport.getPropertyGetter(ExampleBean.class, "name");
        assertEquals("getName", name.getName());
    }

    public void testIsGetter() throws Exception {
        ExampleBean bean = new ExampleBean();

        Method name = bean.getClass().getMethod("getName", (Class<?>[])null);
        assertEquals(true, IntrospectionSupport.isGetter(name));
        assertEquals(false, IntrospectionSupport.isSetter(name));

        Method price = bean.getClass().getMethod("getPrice", (Class<?>[])null);
        assertEquals(true, IntrospectionSupport.isGetter(price));
        assertEquals(false, IntrospectionSupport.isSetter(price));
    }

    public void testIsSetter() throws Exception {
        ExampleBean bean = new ExampleBean();

        Method name = bean.getClass().getMethod("setName", String.class);
        assertEquals(false, IntrospectionSupport.isGetter(name));
        assertEquals(true, IntrospectionSupport.isSetter(name));

        Method price = bean.getClass().getMethod("setPrice", double.class);
        assertEquals(false, IntrospectionSupport.isGetter(price));
        assertEquals(true, IntrospectionSupport.isSetter(price));
    }

    public void testOtherIsGetter() throws Exception {
        OtherExampleBean bean = new OtherExampleBean();

        Method customerId = bean.getClass().getMethod("getCustomerId", (Class<?>[])null);
        assertEquals(true, IntrospectionSupport.isGetter(customerId));
        assertEquals(false, IntrospectionSupport.isSetter(customerId));

        Method goldCustomer = bean.getClass().getMethod("isGoldCustomer", (Class<?>[])null);
        assertEquals(true, IntrospectionSupport.isGetter(goldCustomer));
        assertEquals(false, IntrospectionSupport.isSetter(goldCustomer));

        Method silverCustomer = bean.getClass().getMethod("isSilverCustomer", (Class<?>[])null);
        assertEquals(true, IntrospectionSupport.isGetter(silverCustomer));
        assertEquals(false, IntrospectionSupport.isSetter(silverCustomer));

        Method company = bean.getClass().getMethod("getCompany", (Class<?>[])null);
        assertEquals(true, IntrospectionSupport.isGetter(company));
        assertEquals(false, IntrospectionSupport.isSetter(company));

        Method setupSomething = bean.getClass().getMethod("setupSomething", Object.class);
        assertEquals(false, IntrospectionSupport.isGetter(setupSomething));
        assertEquals(false, IntrospectionSupport.isSetter(setupSomething));
    }

    public void testOtherIsSetter() throws Exception {
        OtherExampleBean bean = new OtherExampleBean();

        Method customerId = bean.getClass().getMethod("setCustomerId", int.class);
        assertEquals(false, IntrospectionSupport.isGetter(customerId));
        assertEquals(true, IntrospectionSupport.isSetter(customerId));

        Method goldCustomer = bean.getClass().getMethod("setGoldCustomer", boolean.class);
        assertEquals(false, IntrospectionSupport.isGetter(goldCustomer));
        assertEquals(true, IntrospectionSupport.isSetter(goldCustomer));

        Method silverCustomer = bean.getClass().getMethod("setSilverCustomer", Boolean.class);
        assertEquals(false, IntrospectionSupport.isGetter(silverCustomer));
        assertEquals(true, IntrospectionSupport.isSetter(silverCustomer));

        Method company = bean.getClass().getMethod("setCompany", String.class);
        assertEquals(false, IntrospectionSupport.isGetter(company));
        assertEquals(true, IntrospectionSupport.isSetter(company));

        Method setupSomething = bean.getClass().getMethod("setupSomething", Object.class);
        assertEquals(false, IntrospectionSupport.isGetter(setupSomething));
        assertEquals(false, IntrospectionSupport.isSetter(setupSomething));
    }
}

