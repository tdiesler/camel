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
package org.apache.camel.component.quartz;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.CamelContext;
import org.apache.camel.StartupListener;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 * A <a href="http://camel.apache.org/quartz.html">Quartz Component</a>
 * <p/>
 * For a brief tutorial on setting cron expression see
 * <a href="http://www.opensymphony.com/quartz/wikidocs/CronTriggers%20Tutorial.html">Quartz cron tutorial</a>.
 *
 * @version $Revision:520964 $
 */
public class QuartzComponent extends DefaultComponent implements StartupListener {
    private static final transient Log LOG = LogFactory.getLog(QuartzComponent.class);
    private static final AtomicInteger JOBS = new AtomicInteger();
    private static Scheduler scheduler;
    private SchedulerFactory factory;
    private Properties properties;
    private String propertiesFile;
    private int startDelayedSeconds;
    private boolean autoStartScheduler = true;

    public QuartzComponent() {
    }

    public QuartzComponent(final CamelContext context) {
        super(context);
    }

    @Override
    protected QuartzEndpoint createEndpoint(final String uri, final String remaining, final Map<String, Object> parameters) throws Exception {
        QuartzEndpoint answer = new QuartzEndpoint(uri, this);

        // lets split the remaining into a group/name
        URI u = new URI(uri);
        String path = ObjectHelper.after(u.getPath(), "/");
        String host = u.getHost();
        String cron = getAndRemoveParameter(parameters, "cron", String.class);
        Boolean fireNow = getAndRemoveParameter(parameters, "fireNow", Boolean.class, Boolean.FALSE);

        // group can be optional, if so set it to Camel
        String name;
        String group;
        if (ObjectHelper.isNotEmpty(path) && ObjectHelper.isNotEmpty(host)) {
            group = host;
            name = path;
        } else {
            group = "Camel";
            name = host;
        }

        Map<String, Object> triggerParameters = IntrospectionSupport.extractProperties(parameters, "trigger.");
        Map<String, Object> jobParameters = IntrospectionSupport.extractProperties(parameters, "job.");

        // create the trigger either cron or simple
        Trigger trigger;
        if (ObjectHelper.isNotEmpty(cron)) {
            trigger = createCronTrigger(cron);
        } else {
            trigger = new SimpleTrigger();
            if (fireNow) {
                String intervalString = (String) triggerParameters.get("repeatInterval");
                if (intervalString != null) {
                    long interval = Long.valueOf(intervalString);
                    trigger.setStartTime(new Date(System.currentTimeMillis() - interval));
                }
            }
        }
        answer.setTrigger(trigger);

        trigger.setName(name);
        trigger.setGroup(group);

        setProperties(trigger, triggerParameters);
        setProperties(answer.getJobDetail(), jobParameters);

        return answer;
    }

    protected CronTrigger createCronTrigger(String path) throws ParseException {
        // replace + back to space so its a cron expression
        path = path.replaceAll("\\+", " ");
        CronTrigger cron = new CronTrigger();
        cron.setCronExpression(path);
        return cron;
    }

    public void onCamelContextStarted(CamelContext camelContext) throws Exception {
        // if not configure to auto start then don't start it
        if (!isAutoStartScheduler()) {
            LOG.info("QuartzComponent configured to not auto start Quartz scheduler.");
            return;
        }

        // only start scheduler when CamelContext have finished starting
        startScheduler();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        if (scheduler == null) {
            scheduler = getScheduler();
        }
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        if (scheduler != null) {
            int number = JOBS.get();
            if (number > 0) {
                LOG.info("Cannot shutdown Quartz scheduler: " + scheduler.getSchedulerName() + " as there are still " + number + " jobs registered.");
            } else {
                // no more jobs then shutdown the scheduler
                LOG.info("There are no more jobs registered, so shutting down Quartz scheduler: " + scheduler.getSchedulerName());
                scheduler.shutdown();
                scheduler = null;
            }
        }
    }

    public void addJob(JobDetail job, Trigger trigger) throws SchedulerException {
        JOBS.incrementAndGet();

        if (getScheduler().getTrigger(trigger.getName(), trigger.getGroup()) == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Adding job using trigger: " + trigger.getGroup() + "/" + trigger.getName());
            }
            getScheduler().scheduleJob(job, trigger);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resuming job using trigger: " + trigger.getGroup() + "/" + trigger.getName());
            }
            getScheduler().resumeTrigger(trigger.getName(), trigger.getGroup());
        }
    }

    public void removeJob(JobDetail job, Trigger trigger) throws SchedulerException {
        JOBS.decrementAndGet();

        if (isClustered()) {
            // do not remove jobs which are clustered, as we want the jobs to continue running on the other nodes
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cannot removing job using trigger: " + trigger.getGroup() + "/" + trigger.getName() + " as the JobStore is clustered.");
            }
            return;
        }

        // only unschedule volatile jobs
        if (job.isVolatile()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Removing job using trigger: " + trigger.getGroup() + "/" + trigger.getName());
            }
            getScheduler().unscheduleJob(trigger.getName(), trigger.getGroup());
        } else {
            // but pause jobs so we can resume them if the application restarts
            if (LOG.isDebugEnabled()) {
                LOG.debug("Pausing job using trigger: " + trigger.getGroup() + "/" + trigger.getName());
            }
            getScheduler().pauseTrigger(trigger.getName(), trigger.getGroup());
        }
    }

    /**
     * To force shutdown the quartz scheduler
     *
     * @throws SchedulerException can be thrown if error shutting down
     */
    public void shutdownScheduler() throws SchedulerException {
        if (scheduler != null) {
            LOG.info("Forcing shutdown of Quartz scheduler: " + scheduler.getSchedulerName());
            scheduler.shutdown();
            scheduler = null;
        }
    }

    /**
     * Is the quartz scheduler clustered?
     */
    public boolean isClustered() throws SchedulerException {
        return getScheduler().getMetaData().isJobStoreClustered();
    }

    /**
     * To force starting the quartz scheduler
     *
     * @throws SchedulerException can be thrown if error starting
     */
    public void startScheduler() throws SchedulerException {
        if (!scheduler.isStarted()) {
            if (getStartDelayedSeconds() > 0) {
                LOG.info("Starting Quartz scheduler: " + scheduler.getSchedulerName() + " delayed: " + getStartDelayedSeconds() + " seconds.");
                scheduler.startDelayed(getStartDelayedSeconds());
            } else {
                LOG.info("Starting Quartz scheduler: " + scheduler.getSchedulerName());
                scheduler.start();
            }
        }
    }

    // Properties
    // -------------------------------------------------------------------------

    public SchedulerFactory getFactory() throws SchedulerException {
        if (factory == null) {
            factory = createSchedulerFactory();
        }
        return factory;
    }

    public void setFactory(final SchedulerFactory factory) {
        this.factory = factory;
    }

    public synchronized Scheduler getScheduler() throws SchedulerException {
        if (scheduler == null) {
            scheduler = createScheduler();
        }
        return scheduler;
    }

    public void setScheduler(final Scheduler scheduler) {
        QuartzComponent.scheduler = scheduler;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getPropertiesFile() {
        return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    public int getStartDelayedSeconds() {
        return startDelayedSeconds;
    }

    public void setStartDelayedSeconds(int startDelayedSeconds) {
        this.startDelayedSeconds = startDelayedSeconds;
    }

    public boolean isAutoStartScheduler() {
        return autoStartScheduler;
    }

    public void setAutoStartScheduler(boolean autoStartScheduler) {
        this.autoStartScheduler = autoStartScheduler;
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    protected Properties loadProperties() throws SchedulerException {
        Properties answer = getProperties();
        if (answer == null && getPropertiesFile() != null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Loading Quartz properties file from classpath: " + getPropertiesFile());
            }
            InputStream is = getCamelContext().getClassResolver().loadResourceAsStream(getPropertiesFile());
            if (is == null) {
                throw new SchedulerException("Quartz properties file not found in classpath: " + getPropertiesFile());
            }
            answer = new Properties();
            try {
                answer.load(is);
            } catch (IOException e) {
                throw new SchedulerException("Error loading Quartz properties file from classpath: " + getPropertiesFile(), e);
            }
        }
        return answer;
    }

    protected SchedulerFactory createSchedulerFactory() throws SchedulerException {
        Properties prop = loadProperties();
        if (prop != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating SchedulerFactory with properties: " + prop);
            }
            return new StdSchedulerFactory(prop);
        } else {
            return new StdSchedulerFactory();
        }
    }

    protected Scheduler createScheduler() throws SchedulerException {
        Scheduler scheduler = getFactory().getScheduler();
        scheduler.getContext().put(QuartzConstants.QUARTZ_CAMEL_CONTEXT, getCamelContext());
        return scheduler;
    }
}
