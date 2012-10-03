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
package org.apache.camel.example.cdi.one;

import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.cdi.internal.CamelExtension;
import org.apache.camel.example.cdi.ArchiveUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;

/**
 *  Factory to create archive according to the container selected
 */
public class DeploymentFactory {

    @Deployment
    public static Archive<?> createArchive() {

        String deploymentType = System.getProperty("arquillian");
        Archive<?> archive = null;

        // TODO FIND A BETTER WAY TO PASS PACKAGES
        String[] packages = {"org.apache.camel.example.cdi","org.apache.camel.example.cdi.one"};

        System.out.println("Deployment type : " + deploymentType);

        if (deploymentType.equals("weld-ee-embedded-1.1")) {
            archive = ArchiveUtil.createWeldArchive(packages);

        } else if (deploymentType.equals("jbossas-managed")) {
            archive =  ArchiveUtil.createJBossASArchive(packages);
        }
        return archive;
    }

}
