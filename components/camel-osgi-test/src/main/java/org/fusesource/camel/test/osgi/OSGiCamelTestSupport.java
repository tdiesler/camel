/**
 * Copyright (C) 2010 FuseSource, Corp. All rights reserved.
 * http://fusesource.com
 *
 * The software in this package is published under the terms of the CDDL license
 * a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.camel.test.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.osgi.CamelContextFactory;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.options.UrlReference;
import org.ops4j.store.Store;
import org.ops4j.store.StoreFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.profile;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.scanFeatures;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.workingDirectory;

/**
 * {@link CamelTestSupport} for testing on OSGi runtimes.
 *
 * @version $Revision$
 */
public class OSGiCamelTestSupport extends CamelTestSupport {

    private String loggingLevel = "INFO";
    private boolean useFelix = true;
    private boolean useEquinox = true;

    // TODO: add support for fusesource maven repo
    // http://repo.fusesource.com/nexus/content/groups/public

    @Inject
    protected BundleContext bundleContext;

    protected Bundle getInstalledBundle(String symbolicName) {
        for (Bundle b : bundleContext.getBundles()) {
            if (b.getSymbolicName().equals(symbolicName)) {
                return b;
            }
        }
        for (Bundle b : bundleContext.getBundles()) {
            log.debug("Bundle: " + b.getSymbolicName());
        }
        throw new IllegalArgumentException("Bundle " + symbolicName + " does not exist");
    }

    protected CamelContext createCamelContext() throws Exception {
        log.info("Application installed as bundle id: " + bundleContext.getBundle().getBundleId());

        setThreadContextClassLoader();

        CamelContextFactory factory = new CamelContextFactory();
        factory.setBundleContext(bundleContext);
        factory.setRegistry(createRegistry());
        return factory.createContext();
    }

    private void setThreadContextClassLoader() {
        // set the thread context classloader to use this current classloader
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    }

    private static UrlReference getCamelKarafFeatureUrl() {
        // TODO: offer a way to define camel version
        return mavenBundle().groupId("org.apache.camel.karaf").
                artifactId("apache-camel").version("2.x-fuse-SNAPSHOT").type("xml/features");
    }

    private static UrlReference getKarafFeatureUrl() {
        // TODO: offer a way to define karaf version
        String type = "xml/features";
        return mavenBundle().groupId("org.apache.karaf").
                artifactId("apache-karaf").version("2.1.2").type(type);
    }

    protected static UrlProvisionOption bundle(final InputStream stream) throws IOException {
        Store<InputStream> store = StoreFactory.defaultStore();
        return new UrlProvisionOption(store.getLocation(store.store(stream)).toURL().toExternalForm());
    }

    public void setLoggingLevel(String loggingLevel) {
        this.loggingLevel = loggingLevel;
    }

    public void setUseFelix(boolean useFelix) {
        this.useFelix = useFelix;
    }

    public void setUseEquinox(boolean useEquinox) {
        this.useEquinox = useEquinox;
    }

    /**
     * Allow end users to specify a number of features to use
     */
    public String[] getFeatures() {
        return null;
    }

    private Option getFelixRuntimeOption() {
        return useFelix ? felix() : null;
    }

    private Option getEquinoxRuntimeOption() {
        return useEquinox ? equinox() : null;
    }

    private String[] assembleFeatures() {
        List<String> features = new ArrayList<String>();
        // mandatory features
        features.add("camel-core");
        features.add("camel-test");
        features.add("camel-spring");
        features.add("camel-blueprint");
        // add any optional features
        if (getFeatures() != null) {
            for (String s : getFeatures()) {
                features.add(s);
            }
        }
        return features.toArray(new String[features.size()]);
    }

    /**
     * Allows end users to setup an settings before the OSGi configuration takes place.
     * <p/>
     * For example you can configure which osgi runtimes to use for testing.
     */
    public void setupSettings() {
        // noop
    }

    @Configuration
    public Option[] configure() throws Exception {
        // allow end user to customize settings before we configure
        setupSettings();

        Option[] options = options(

            // install the spring dm profile
            profile("spring.dm").version("1.2.0"),

            // this is how you set the default log level when using pax logging (logProfile)
            org.ops4j.pax.exam.CoreOptions.systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value(loggingLevel),

            // this is the test kit bundle we need to install and start
            // TODO: Use version from pom
            mavenBundle("org.apache.camel", "camel-osgi-test").version("2.x-fuse-SNAPSHOT").start(),

            // using the features to install the camel components
            scanFeatures(getCamelKarafFeatureUrl(), assembleFeatures()),

            workingDirectory("target/paxrunner/"),

            getFelixRuntimeOption(), getEquinoxRuntimeOption());

        return options;
    }

}
