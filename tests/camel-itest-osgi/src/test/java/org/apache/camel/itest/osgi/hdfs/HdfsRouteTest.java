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
package org.apache.camel.itest.osgi.hdfs;

import java.io.File;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.itest.osgi.OSGiIntegrationTestSupport;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Constants;

import static org.apache.hadoop.io.SequenceFile.createWriter;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.CoreOptions.scanFeatures;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.swissbox.tinybundles.core.TinyBundles.newBundle;

@RunWith(JUnit4TestRunner.class)
@Ignore("karaf-pax-exam have trouble to modify the test prob bundle, We need to revisit this test later.")
public class HdfsRouteTest extends OSGiIntegrationTestSupport {
    //Hadoop doesn't run on IBM JDK
    private static final boolean SKIP = System.getProperty("java.vendor").contains("IBM");

    @Test
    public void testReadString() throws Exception {
        if (SKIP) {
            return;
        }

        final Path file = new Path(new File("../../../../target/test/test-camel-string").getAbsolutePath());
        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
        //conf.setClassLoader(this.getClass().getClassLoader());
        // add the default configure into the resource
        conf.addResource(HdfsRouteTest.class.getResourceAsStream("/core-default.xml"));
        FileSystem fs1 = FileSystem.get(file.toUri(), conf);
        SequenceFile.Writer writer = createWriter(fs1, conf, file, NullWritable.class, Text.class);
        NullWritable keyWritable = NullWritable.get();
        Text valueWritable = new Text();
        String value = "CIAO!";
        valueWritable.set(value);
        writer.append(keyWritable, valueWritable);
        writer.sync();
        writer.close();

        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("hdfs:///" + file.toUri() + "?fileSystemType=LOCAL&fileType=SEQUENCE_FILE&initialDelay=0").to("mock:result");
            }
        });
        context.start();

        MockEndpoint resultEndpoint = context.getEndpoint("mock:result", MockEndpoint.class);
        resultEndpoint.expectedMessageCount(1);
        resultEndpoint.assertIsSatisfied();
    }

    @Configuration
    public static Option[] configure() throws Exception {
        Option[] options = combine(

                getDefaultCamelKarafOptions(),
                // using the features to install the camel components
                scanFeatures(getCamelKarafFeatureUrl(), "camel-hdfs"),
                //TODO need to find a way to override the test prob bundle
                provision(newBundle()
                                .add("core-default.xml", HdfsRouteTest.class.getResource("/core-default.xml"))
                                .set(Constants.BUNDLE_SYMBOLICNAME, "CamelHdfsTestBundle")
                                .set(Constants.DYNAMICIMPORT_PACKAGE, "*")
                                .build()));

        return options;
    }
}
