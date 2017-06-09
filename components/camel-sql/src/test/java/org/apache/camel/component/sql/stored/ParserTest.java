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
package org.apache.camel.component.sql.stored;

import java.math.BigInteger;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.sql.stored.template.TemplateParser;
import org.apache.camel.component.sql.stored.template.ast.InputParameter;
import org.apache.camel.component.sql.stored.template.ast.OutParameter;
import org.apache.camel.component.sql.stored.template.ast.ParseRuntimeException;
import org.apache.camel.component.sql.stored.template.ast.Template;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Assert;
import org.junit.Test;

public class ParserTest extends CamelTestSupport {

    TemplateParser parser;



    @Override
    protected void startCamelContext() throws Exception {
        super.startCamelContext();
        parser = new TemplateParser(context.getClassResolver());
    }

    @Test
    public void shouldParseOk() {
        Template template = parser.parseTemplate("addnumbers(INTEGER ${header.header1},"
                + "VARCHAR ${property.property1},BIGINT ${header.header2},OUT INTEGER header1)");

        Assert.assertEquals("addnumbers", template.getProcedureName());
        Assert.assertEquals(4, template.getParameterList().size());

        Exchange exchange = createExchangeWithBody(null);
        exchange.getIn().setHeader("header1", 1);
        exchange.setProperty("property1", "constant string");
        exchange.getIn().setHeader("header2", BigInteger.valueOf(2));

        InputParameter param1 = (InputParameter) template.getParameterList().get(0);
        Assert.assertEquals("_0", param1.getName());
        Assert.assertEquals(Types.INTEGER, param1.getSqlType());
        Assert.assertEquals(1, param1.getValueExtractor().eval(exchange, null));

        InputParameter param2 = (InputParameter) template.getParameterList().get(1);
        Assert.assertEquals("_1", param2.getName());
        Assert.assertEquals(Types.VARCHAR, param2.getSqlType());
        Assert.assertEquals("constant string", param2.getValueExtractor().eval(exchange, null));

        InputParameter param3 = (InputParameter) template.getParameterList().get(2);
        Assert.assertEquals("_2", param3.getName());
        Assert.assertEquals(Types.BIGINT, param3.getSqlType());
        Assert.assertEquals(BigInteger.valueOf(2L), param3.getValueExtractor().eval(exchange, null));

        OutParameter sptpOutputNode = (OutParameter) template.getParameterList().get(3);
        Assert.assertEquals("_3", sptpOutputNode.getName());
        Assert.assertEquals(Types.INTEGER, sptpOutputNode.getSqlType());
        Assert.assertEquals("header1", sptpOutputNode.getOutValueMapKey());
    }

    @Test(expected = ParseRuntimeException.class)
    public void noOutputParameterShouldFail() {
        parser.parseTemplate("ADDNUMBERS2"
                + "(INTEGER VALUE1 ${header.v1},INTEGER VALUE2 ${header.v2})");
    }

    @Test(expected = ParseRuntimeException.class)
    public void unexistingTypeShouldFail() {
        parser.parseTemplate("ADDNUMBERS2"
                + "(XML VALUE1 ${header.v1},OUT INTEGER VALUE2 ${header.v2})");
    }

    @Test
    public void nestedSimpleExpression() {
        Exchange exchange = createExchangeWithBody(1);
        exchange.getIn().setHeader("foo", 1);
        exchange.getIn().setHeader("bar", 3);
        Template template = parser.parseTemplate("ADDNUMBERS2(INTEGER ${header.foo},INTEGER ${header.bar})");
        assertEquals(1, ((InputParameter) template.getParameterList().get(0)).getValueExtractor().eval(exchange, null));
        assertEquals(3, ((InputParameter) template.getParameterList().get(1)).getValueExtractor().eval(exchange, null));
    }

    @Test
    public void vendorSpeficSqlType() {
        Template template = parser.parseTemplate("ADDNUMBERS2(1342 ${header.foo})");
        assertEquals(1342, ((InputParameter) template.getParameterList().get(0)).getSqlType());
    }

    @Test
    public void vendorSpecificNegativeSqlType() {
        Template template = parser.parseTemplate("ADDNUMBERS2(-1342 ${header.foo})");
        assertEquals(-1342, ((InputParameter) template.getParameterList().get(0)).getSqlType());
    }

    @Test
    public void colonInSimple() {
        PropertiesComponent pc = (PropertiesComponent) context.getComponent("properties");
        pc.setLocation("classpath:jndi.properties");
        Exchange exchange = createExchangeWithBody(1);
        Template template = parser.parseTemplate("ADDNUMBERS2(-1342 ${properties:java.naming.factory.initial})");
        assertEquals("org.apache.camel.util.jndi.CamelInitialContextFactory",
            ((InputParameter) template.getParameterList().get(0)).getValueExtractor().eval(exchange, null));
    }

    @Test
    public void colonInLocation() {
        Template template = parser.parseTemplate("ADDNUMBERS2(-1342 :#a:)");
        Exchange exchange = createExchangeWithBody(1);

        Map container = new HashMap();
        container.put("a:", 1);
        assertEquals(1, ((InputParameter) template.getParameterList().get(0)).getValueExtractor().eval(exchange, container));
    }

    @Test
    public void vendorSpecificPositiveSqlTypeOut() {
        Template template = parser.parseTemplate("ADDNUMBERS2(OUT 1342 h1)");
        assertEquals(1342, ((OutParameter) template.getParameterList().get(0)).getSqlType());
    }

    @Test
    public void vendorSpecificNegativeSqlTypeOut() {
        Template template = parser.parseTemplate("ADDNUMBERS2(OUT -1342 h1)");
        assertEquals(-1342, ((OutParameter) template.getParameterList().get(0)).getSqlType());
    }

    @Test
    public void nableIssueSyntax() {
        Map params = new HashMap<>();
        params.put("P_STR_IN", "a");
        Template template = parser.parseTemplate("IBS.\"Z$IMS_INTERFACE_WS\".TEST_STR(VARCHAR :#P_STR_IN,OUT VARCHAR P_STR_OUT)");
        assertEquals("a", ((InputParameter) template.getParameterList().get(0)).getValueExtractor().eval(null, params));
        assertEquals("IBS.\"Z$IMS_INTERFACE_WS\".TEST_STR", template.getProcedureName());
    }

    @Test(expected = ParseRuntimeException.class)
    public void unmappedTypeShouldFaild() {
        parser.parseTemplate("ADDNUMBERS2"
                + "(OTHER VALUE1 ${header.v1},INTEGER VALUE2 ${header.v2})");
    }

    @Test
    public void testParameterNameGiven() {
        Template template = parser.parseTemplate("FOO('p_instance_id' INTEGER ${header.foo})");
        assertEquals("p_instance_id", ((InputParameter) template.getParameterList().get(0)).getName());
    }

    @Test
    public void testParameterVendor() {
        Template template = parser.parseTemplate("FOO('p_instance_id' org.apache.camel.component.sql.stored.CustomType.INTEGER ${header.foo})");
        assertEquals(1, ((InputParameter) template.getParameterList().get(0)).getSqlType());
    }

    @Test
    public void testParameterVendorType() {
        Template template = parser.parseTemplate("FOO('p_instance_id' 2 ${header.foo})");
        assertEquals(2, ((InputParameter) template.getParameterList().get(0)).getSqlType());
    }

    @Test
    public void testParameterTypeName() {
        Template template = parser.parseTemplate("FOO('p_instance_id' 2 'p_2' ${header.foo})");
        assertEquals("p_2", ((InputParameter) template.getParameterList().get(0)).getTypeName());
    }


    @Test
    public void testParameterVendorTypeNegativ() {
        Template template = parser.parseTemplate("FOO('p_instance_id' -2 ${header.foo})");
        assertEquals(-2, ((InputParameter) template.getParameterList().get(0)).getSqlType());
    }

    @Test
    public void testOracleTypesOut() {
        Template template = parser.parseTemplate("FOO(OUT 'p_error_cd' 1 header1)");
        assertEquals(1, ((OutParameter) template.getParameterList().get(0)).getSqlType());
    }

    @Test
    public void testOracleTypesOutParameterVendor() {
        Template template = parser.parseTemplate("FOO(OUT 'p_error_cd' org.apache.camel.component.sql.stored.CustomType.INTEGER header1)");
        assertEquals(1, ((OutParameter) template.getParameterList().get(0)).getSqlType());
    }


    @Test
    public void testOracleTypesNumeric() {
        Template template = parser.parseTemplate("FOO('p_error_cd' org.apache.camel.component.sql.stored.CustomType.INTEGER(10) ${header.foo})");
        assertEquals(Integer.valueOf(10), ((InputParameter) template.getParameterList().get(0)).getScale());
    }


}
