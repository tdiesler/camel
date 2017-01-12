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
package org.apache.camel.maven.bom.generator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.DefaultMavenProjectBuilder;
import org.apache.maven.project.MavenProject;

/**
 * Generate BOM by flattening the current project's dependency management section and applying exclusions.
 *
 * @goal generate
 * @phase validate
 */
public class BomGeneratorMojo extends AbstractMojo {

    /**
     * The maven project.
     *
     * @parameter property="project"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The source pom template file.
     *
     * @parameter default-value="${basedir}/pom.xml"
     */
    protected File sourcePom;

    /**
     * The pom file.
     *
     * @parameter default-value="${project.build.directory}/${project.name}-pom.xml"
     */
    protected File targetPom;


    /**
     * The user configuration
     *
     * @parameter
     * @readonly
     */
    protected DependencySet dependencies;

    /**
     * The conflict checks configured by the user
     *
     * @parameter
     * @readonly
     */
    protected ExternalBomConflictCheckSet checkConflicts;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
     * @required
     * @readonly
     */
    protected ArtifactFactory artifactFactory;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
     * @required
     * @readonly
     */
    protected ArtifactResolver artifactResolver;

    /**
     * List of Remote Repositories used by the resolver
     *
     * @parameter property="project.remoteArtifactRepositories"
     * @readonly
     * @required
     */
    protected List remoteRepositories;

    /**
     * Location of the local repository.
     *
     * @parameter property="localRepository"
     * @readonly
     * @required
     */
    protected ArtifactRepository localRepository;

    /**
     * Used to build a maven projects from artifacts in the remote repository.
     *
     * @component role="org.apache.maven.project.MavenProjectBuilder"
     * @required
     * @readonly
     */
    protected DefaultMavenProjectBuilder projectBuilder;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            DependencyManagement mng = project.getDependencyManagement();

            List<Dependency> filteredDependencies = enhance(filter(mng.getDependencies()));

            Set<ComparisonKey> externallyManagedDependencies = getExternallyManagedDependencies();
            checkConflictsWithExternalBoms(filteredDependencies, externallyManagedDependencies);

            Document pom = loadBasePom();

            // transform
            overwriteDependencyManagement(pom, filteredDependencies);

            writePom(pom);

        } catch (MojoFailureException ex) {
            throw ex;
        } catch (MojoExecutionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MojoExecutionException("Cannot generate the output BOM file", ex);
        }
    }

    private List<Dependency> enhance(List<Dependency> dependencyList) {

        for (Dependency dep : dependencyList) {
            if (dep.getGroupId().startsWith(project.getGroupId()) && project.getVersion().equals(dep.getVersion())) {
                dep.setVersion("${project.version}");
            }
        }

        return dependencyList;
    }

    private List<Dependency> filter(List<Dependency> dependencyList) {
        return this.filter(dependencyList, this.dependencies, this.project.getArtifactId());
    }

    private List<Dependency> filter(List<Dependency> dependencyList, DependencySet dependencies, String bomName) {
        List<Dependency> outDependencies = new ArrayList<>();

        DependencyMatcher inclusions = new DependencyMatcher(dependencies.getIncludes());
        DependencyMatcher exclusions = new DependencyMatcher(dependencies.getExcludes());

        for (Dependency dep : dependencyList) {
            boolean accept = inclusions.matches(dep) && !exclusions.matches(dep);
            getLog().debug(dep + (accept ? " included in the BOM" : " excluded from BOM") + " " + bomName);

            if (accept) {
                outDependencies.add(dep);
            }
        }

        Collections.sort(outDependencies, (d1, d2) -> (d1.getGroupId() + ":" + d1.getArtifactId()).compareTo(d2.getGroupId() + ":" + d2.getArtifactId()));

        return outDependencies;
    }

    private Document loadBasePom() throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document pom = builder.parse(sourcePom);

        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile("/project/parent/version");

        Node node = (Node) expr.evaluate(pom, XPathConstants.NODE);
        if (node != null && node.getTextContent() != null && node.getTextContent().trim().equals("${project.version}")) {
            node.setTextContent(project.getVersion());
        }

        return pom;
    }

    private void writePom(Document pom) throws Exception {
        XPathExpression xpath = XPathFactory.newInstance().newXPath().compile("//text()[normalize-space(.) = '']");
        NodeList emptyNodes = (NodeList) xpath.evaluate(pom, XPathConstants.NODESET);

        // Remove empty text nodes
        for (int i = 0; i < emptyNodes.getLength(); i++) {
            Node emptyNode = emptyNodes.item(i);
            emptyNode.getParentNode().removeChild(emptyNode);
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(pom);

        targetPom.getParentFile().mkdirs();

        String content;
        try (StringWriter out = new StringWriter()) {
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
            content = out.toString();
        }

        // Fix header formatting problem
        content = content.replaceFirst("-->", "-->\n");
        writeFileIfChanged(content, targetPom);
    }

    private void writeFileIfChanged(String content, File file) throws IOException {
        boolean write = true;

        if (file.exists()) {
            try (FileReader fr = new FileReader(file)) {
                String oldContent = IOUtils.toString(fr);
                if (!content.equals(oldContent)) {
                    getLog().debug("Writing new file " + file.getAbsolutePath());
                    fr.close();
                } else {
                    getLog().debug("File " + file.getAbsolutePath() + " left unchanged");
                    write = false;
                }
            }
        } else {
            File parent = file.getParentFile();
            parent.mkdirs();
        }

        if (write) {
            try (FileWriter fw = new FileWriter(file)) {
                IOUtils.write(content, fw);
            }
        }
    }


    private void overwriteDependencyManagement(Document pom, List<Dependency> dependencies) throws Exception {

        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile("/project/dependencyManagement/dependencies");

        NodeList nodes = (NodeList) expr.evaluate(pom, XPathConstants.NODESET);
        if (nodes.getLength() == 0) {
            throw new IllegalStateException("No dependencies found in the dependencyManagement section of the current pom");
        }

        Node dependenciesSection = nodes.item(0);
        // cleanup the dependency management section
        while (dependenciesSection.hasChildNodes()) {
            Node child = dependenciesSection.getFirstChild();
            dependenciesSection.removeChild(child);
        }

        for (Dependency dep : dependencies) {
            Element dependencyEl = pom.createElement("dependency");

            Element groupIdEl = pom.createElement("groupId");
            groupIdEl.setTextContent(dep.getGroupId());
            dependencyEl.appendChild(groupIdEl);

            Element artifactIdEl = pom.createElement("artifactId");
            artifactIdEl.setTextContent(dep.getArtifactId());
            dependencyEl.appendChild(artifactIdEl);

            Element versionEl = pom.createElement("version");
            versionEl.setTextContent(dep.getVersion());
            dependencyEl.appendChild(versionEl);

            if (!"jar".equals(dep.getType())) {
                Element typeEl = pom.createElement("type");
                typeEl.setTextContent(dep.getType());
                dependencyEl.appendChild(typeEl);
            }

            if (dep.getClassifier() != null) {
                Element classifierEl = pom.createElement("classifier");
                classifierEl.setTextContent(dep.getClassifier());
                dependencyEl.appendChild(classifierEl);
            }

            if (dep.getScope() != null && !"compile".equals(dep.getScope())) {
                Element scopeEl = pom.createElement("scope");
                scopeEl.setTextContent(dep.getScope());
                dependencyEl.appendChild(scopeEl);
            }

            if (dep.getExclusions() != null && !dep.getExclusions().isEmpty()) {

                Element exclsEl = pom.createElement("exclusions");

                for (Exclusion e : dep.getExclusions()) {
                    Element exclEl = pom.createElement("exclusion");

                    Element groupIdExEl = pom.createElement("groupId");
                    groupIdExEl.setTextContent(e.getGroupId());
                    exclEl.appendChild(groupIdExEl);

                    Element artifactIdExEl = pom.createElement("artifactId");
                    artifactIdExEl.setTextContent(e.getArtifactId());
                    exclEl.appendChild(artifactIdExEl);

                    exclsEl.appendChild(exclEl);
                }

                dependencyEl.appendChild(exclsEl);
            }


            dependenciesSection.appendChild(dependencyEl);
        }


    }

    private void checkConflictsWithExternalBoms(Collection<Dependency> dependencies, Set<ComparisonKey> external) throws MojoFailureException {
        Map<ComparisonKey, ComparisonKey> errors = new TreeMap<>();
        for (Dependency d : dependencies) {
            ComparisonKey key = comparisonKey(d);
            Optional<ComparisonKey> matchedKey = external.stream()
                .filter((k) -> !d.getVersion().contains("redhat") && k.equals(key) && k.isConflicting(key))
                .findFirst();
            if (matchedKey.isPresent()) {
                errors.put(key, matchedKey.get());
            }
        }

        if (errors.size() > 0) {
            StringBuilder msg = new StringBuilder();
            msg.append("Found ").append(errors.size()).append(" conflicts between the current managed dependencies and the external BOMS:\n");
            for (ComparisonKey error : errors.keySet()) {
                msg.append(" - ").append(error).append(" <> ").append(errors.get(error)).append("\n");
            }

            throw new MojoFailureException(msg.toString());
        }
    }

    private Set<ComparisonKey> getExternallyManagedDependencies() throws Exception {
        Set<ComparisonKey> provided = new HashSet<>();
        if (checkConflicts != null && checkConflicts.getBoms() != null) {
            for (ExternalBomConflictCheck check : checkConflicts.getBoms()) {
                Set<ComparisonKey> bomProvided = getProvidedDependencyManagement(check.getGroupId(), check.getArtifactId(), check.getVersion(), check.getDependencies());
                provided.addAll(bomProvided);
            }
        }

        return provided;
    }

    private Set<ComparisonKey> getProvidedDependencyManagement(String groupId, String artifactId, String version, DependencySet dependencyFilter) throws Exception {
        Artifact bom = resolveArtifact(groupId, artifactId, version, "pom");
        MavenProject bomProject = projectBuilder.buildFromRepository(bom, remoteRepositories, localRepository);

        Set<ComparisonKey> provided = new HashSet<>();
        if (bomProject.getDependencyManagement() != null && bomProject.getDependencyManagement().getDependencies() != null) {
            List<Dependency> dependencies = bomProject.getDependencyManagement().getDependencies();
            if (dependencyFilter != null) {
                dependencies = filter(dependencies, dependencyFilter, artifactId);
            }
            for (Dependency dep : dependencies) {
                provided.add(comparisonKey(dep));
            }
        }

        return provided;
    }

    private ComparisonKey comparisonKey(Dependency dependency) {
        String version = dependency.getVersion().replaceAll("(\\.redhat|-redhat).*", "");
        return new ComparisonKey(dependency.getGroupId(), dependency.getArtifactId(), version);
    }

    private Artifact resolveArtifact(String groupId, String artifactId, String version, String type) throws Exception {

        Artifact art = artifactFactory.createArtifact(groupId, artifactId, version, "runtime", type);

        artifactResolver.resolve(art, remoteRepositories, localRepository);

        return art;
    }

    private final class ComparisonKey implements Comparable<ComparisonKey> {
        private String groupId;
        private String artifactId;
        private String version;

        private ComparisonKey(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        private boolean isConflicting(ComparisonKey other) {
            getLog().debug("Comparing [" + this.toString() + "] with [" + other.toString() + "]");

            return !other.version.equals(this.version);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ComparisonKey other = (ComparisonKey) o;

            if (!groupId.equals(other.getGroupId())) {
                return false;
            }
            return artifactId.equals(other.getArtifactId());
        }

        @Override
        public int hashCode() {
            int result = groupId.hashCode();
            result = 31 * result + artifactId.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return String.format("%s:%s:%s", groupId, artifactId, version);
        }

        @Override
        public int compareTo(ComparisonKey other) {
            return this.toString().compareTo(other.toString());
        }
    }

}
