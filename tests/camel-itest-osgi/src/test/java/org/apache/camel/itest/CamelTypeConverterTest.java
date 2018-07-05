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
package org.apache.camel.itest;

import org.apache.camel.CamelContext;
import org.apache.camel.itest.osgi.blueprint.OSGiBlueprintTestSupport;
import org.apache.camel.itest.typeconverter.MyConverter;
import org.apache.camel.util.ObjectHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Constants;

import java.io.File;
import java.net.URL;

import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class CamelTypeConverterTest extends OSGiBlueprintTestSupport {

    @Test
    public void testTypeConverterInSameBundleAsCamelRoute() throws Exception {
        // lookup Camel from OSGi
        CamelContext camel = getOsgiService(CamelContext.class);

        boolean foundFromPojoToString = false;
        boolean foundFromStringToPojo = false;

        for( Class[] clazzes : camel.getTypeConverterRegistry().listAllTypeConvertersFromTo() ) {
            if ( clazzes[0].getName().equals("org.apache.camel.itest.Pojo") && clazzes[1].getName().equals("java.lang.String") ){
                foundFromPojoToString = true;
            }
            if ( clazzes[0].getName().equals("java.lang.String") && clazzes[1].getName().equals("org.apache.camel.itest.Pojo") ){
                foundFromStringToPojo = true;
            }
        }

        Assert.assertTrue( foundFromPojoToString && foundFromStringToPojo );
    }

    @Configuration
    public static Option[] configure() throws Exception {
        URL converterDeclaration = ObjectHelper.loadResourceAsURL("org/apache/camel/itest/TypeConverter", CamelTypeConverterTest.class.getClassLoader());
        String name = "CamelTypeConverterTest";

        Option[] options = combine(
                getDefaultCamelKarafOptions(),

                // using the features to install the camel components
                loadCamelFeatures("camel-blueprint"),

                KarafDistributionOption.replaceConfigurationFile("etc/org.ops4j.pax.logging.cfg", new File("src/test/resources/org/apache/camel/itest/org.ops4j.pax.logging.cfg")),

                bundle(TinyBundles.bundle()
                        .add("OSGI-INF/blueprint/test.xml", CamelTypeConverterTest.class.getResource("TypeConverterBlueprintRouter.xml"))
                        .add("META-INF/services/org/apache/camel/TypeConverter", converterDeclaration)
                        .add(MyConverter.class)
                        .add(Pojo.class)
                        .set(Constants.BUNDLE_SYMBOLICNAME, name)
                        .set("Manifest-Version", "2")
                        .set("Bundle-ManifestVersion", "2")
                        .set("Bundle-SymbolicName", name)
                        .set("Bundle-Version", "1.0.0")
                        .set(Constants.IMPORT_PACKAGE, "org.apache.camel")
                        .set(Constants.EXPORT_PACKAGE, "org.apache.camel.itest")
                        .set(Constants.DYNAMICIMPORT_PACKAGE, "*")
                        .build()).noStart()
        );

        return options;
    }
}