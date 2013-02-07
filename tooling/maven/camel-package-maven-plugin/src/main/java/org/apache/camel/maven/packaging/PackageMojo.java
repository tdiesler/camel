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
package org.apache.camel.maven.packaging;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Analyses the Camel plugins in a project and generates extra descriptor information for easier auto-discovery in Camel.
 *
 * @goal package
 * @execute phase="compile"
 */
public class PackageMojo extends AbstractMojo {

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The output directory of classes
     *
     * @parameter expression="${project.build.directory}/classes"
     * @readonly
     */
    protected File outDir;


    /**
     * Execute goal.
     *
     * @throws MojoExecutionException execution of the main class or one of the
     *                 threads it generated failed.
     * @throws MojoFailureException something bad happened...
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        File camelMetaDir = new File(outDir, "META-INF/services/org/apache/camel");
        File componentMetaDir = new File(camelMetaDir, "component");
        if (!componentMetaDir.exists()) {
            getLog().warn("No " + componentMetaDir + " directory found. Are you sure you have created a Camel component?");
        } else {
            File[] files = componentMetaDir.listFiles();
            if (files != null) {
                Properties properties = new Properties();
                StringBuilder buffer = new StringBuilder();
                for (File file : files) {
                    String name = file.getName();
                    if (buffer.length() > 0) {
                        buffer.append(" ");
                    }
                    buffer.append(name);
                }
                String names = buffer.toString();
                properties.put("components", names);
                properties.put("groupId", project.getGroupId());
                properties.put("artifactId", project.getArtifactId());
                properties.put("version", project.getVersion());
                properties.put("projectName", project.getName());
                properties.put("projectDescription", project.getDescription());

                File outFile = new File(camelMetaDir, "component.properties");
                try {
                    properties.store(new FileWriter(outFile), "Generated by camel-package-maven-plugin");
                    getLog().info("Generated " + outFile + " containing the camel " + (files.length > 1 ? "components " : "component ") + names);
                } catch (IOException e) {
                    throw new MojoExecutionException("Failed to write properties to " + outFile + ". Reason: " + e, e);
                }
            }
        }
    }

}
