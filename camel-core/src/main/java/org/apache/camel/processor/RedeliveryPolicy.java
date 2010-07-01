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
package org.apache.camel.processor;

import java.io.Serializable;
import java.util.Random;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The policy used to decide how many times to redeliver and the time between
 * the redeliveries before being sent to a <a
 * href="http://camel.apache.org/dead-letter-channel.html">Dead Letter
 * Channel</a>
 * <p>
 * The default values are:
 * <ul>
 *   <li>maximumRedeliveries = 0</li>
 *   <li>redeliveryDelay = 1000L (the initial delay)</li>
 *   <li>maximumRedeliveryDelay = 60 * 1000L</li>
 *   <li>backOffMultiplier = 2</li>
 *   <li>useExponentialBackOff = false</li>
 *   <li>collisionAvoidanceFactor = 0.15d</li>
 *   <li>useCollisionAvoidance = false</li>
 *   <li>retriesExhaustedLogLevel = LoggingLevel.ERROR</li>
 *   <li>retryAttemptedLogLevel = LoggingLevel.DEBUG</li>
 *   <li>logRetryAttempted = true</li>
 *   <li>logRetryStackTrace = false</li>
 *   <li>logStackTrace = true</li>
 *   <li>logHandled = false</li>
 *   <li>logExhausted = true</li>
 * </ul>
 * <p/>
 * Setting the maximumRedeliveries to a negative value such as -1 will then always redeliver (unlimited).
 * Setting the maximumRedeliveries to 0 will disable redelivery.
 * <p/>
 * This policy can be configured either by one of the following two settings:
 * <ul>
 *   <li>using conventional options, using all the options defined above</li>
 *   <li>using delay pattern to declare intervals for delays</li>
 * </ul>
 * <p/>
 * <b>Note:</b> If using delay patterns then the following options is not used (delay, backOffMultiplier, useExponentialBackOff, useCollisionAvoidance)
 * <p/>
 * <b>Using delay pattern</b>:
 * <br/>The delay pattern syntax is: <tt>limit:delay;limit 2:delay 2;limit 3:delay 3;...;limit N:delay N</tt>.
 * <p/>
 * How it works is best illustrate with an example with this pattern: <tt>delayPattern=5:1000;10:5000:20:20000</tt>
 * <br/>The delays will be for attempt in range 0..4 = 0 millis, 5..9 = 1000 millis, 10..19 = 5000 millis, >= 20 = 20000 millis.
 * <p/>
 * If you want to set a starting delay, then use 0 as the first limit, eg: <tt>0:1000;5:5000</tt> will use 1 sec delay
 * until attempt number 5 where it will use 5 seconds going forward.
 *
 * @version $Revision$
 */
public class RedeliveryPolicy implements Cloneable, Serializable {
    protected static transient Random randomNumberGenerator;
    private static final long serialVersionUID = -338222777701473252L;
    private static final transient Log LOG = LogFactory.getLog(RedeliveryPolicy.class);

    protected long redeliveryDelay = 1000L;
    protected int maximumRedeliveries;
    protected long maximumRedeliveryDelay = 60 * 1000L;
    protected double backOffMultiplier = 2;
    protected boolean useExponentialBackOff;
    // +/-15% for a 30% spread -cgs
    protected double collisionAvoidanceFactor = 0.15d;
    protected boolean useCollisionAvoidance;
    protected LoggingLevel retriesExhaustedLogLevel = LoggingLevel.ERROR;
    protected LoggingLevel retryAttemptedLogLevel = LoggingLevel.DEBUG;
    protected boolean logStackTrace = true;
    protected boolean logRetryStackTrace;
    protected boolean logHandled;
    protected boolean logContinued;
    protected boolean logExhausted = true;
    protected boolean logRetryAttempted = true;
    protected String delayPattern;

    public RedeliveryPolicy() {
    }

    @Override
    public String toString() {
        return "RedeliveryPolicy[maximumRedeliveries=" + maximumRedeliveries
            + ", redeliveryDelay=" + redeliveryDelay
            + ", maximumRedeliveryDelay=" + maximumRedeliveryDelay
            + ", retriesExhaustedLogLevel=" + retriesExhaustedLogLevel
            + ", retryAttemptedLogLevel=" + retryAttemptedLogLevel
            + ", logRetryAttempted=" + logRetryAttempted
            + ", logStackTrace=" + logStackTrace
            + ", logRetryStackTrace=" + logRetryStackTrace
            + ", logHandled=" + logHandled
            + ", logContinued=" + logContinued
            + ", logExhausted=" + logExhausted
            + ", useExponentialBackOff="  + useExponentialBackOff
            + ", backOffMultiplier=" + backOffMultiplier
            + ", useCollisionAvoidance=" + useCollisionAvoidance
            + ", collisionAvoidanceFactor=" + collisionAvoidanceFactor
            + ", delayPattern=" + delayPattern + "]";
    }

    public RedeliveryPolicy copy() {
        try {
            return (RedeliveryPolicy)clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Could not clone: " + e, e);
        }
    }

    /**
     * Returns true if the policy decides that the message exchange should be
     * redelivered.
     *
     * @param exchange  the current exchange
     * @param redeliveryCounter  the current retry counter
     * @param retryWhile  an optional predicate to determine if we should redeliver or not
     * @return true to redeliver, false to stop
     */
    public boolean shouldRedeliver(Exchange exchange, int redeliveryCounter, Predicate retryWhile) {
        // predicate is always used if provided
        if (retryWhile != null) {
            return retryWhile.matches(exchange);
        }

        if (getMaximumRedeliveries() < 0) {
            // retry forever if negative value
            return true;
        }
        // redeliver until we hit the max
        return redeliveryCounter <= getMaximumRedeliveries();
    }


    /**
     * Calculates the new redelivery delay based on the last one and then <b>sleeps</b> for the necessary amount of time.
     * <p/>
     * This implementation will block while sleeping.
     *
     * @param redeliveryDelay  previous redelivery delay
     * @param redeliveryCounter  number of previous redelivery attempts
     * @return the calculate delay
     * @throws InterruptedException is thrown if the sleep is interrupted likely because of shutdown
     */
    public long sleep(long redeliveryDelay, int redeliveryCounter) throws InterruptedException {
        redeliveryDelay = calculateRedeliveryDelay(redeliveryDelay, redeliveryCounter);

        if (redeliveryDelay > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sleeping for: " + redeliveryDelay + " millis until attempting redelivery");
            }
            Thread.sleep(redeliveryDelay);
        }
        return redeliveryDelay;
    }

    /**
     * Calculates the new redelivery delay based on the last one
     *
     * @param previousDelay  previous redelivery delay
     * @param redeliveryCounter  number of previous redelivery attempts
     * @return the calculate delay
     */
    public long calculateRedeliveryDelay(long previousDelay, int redeliveryCounter) {
        if (ObjectHelper.isNotEmpty(delayPattern)) {
            // calculate delay using the pattern
            return calculateRedeliverDelayUsingPattern(delayPattern, redeliveryCounter);
        }

        // calculate the delay using the conventional parameters
        long redeliveryDelayResult;
        if (previousDelay == 0) {
            redeliveryDelayResult = redeliveryDelay;
        } else if (useExponentialBackOff && backOffMultiplier > 1) {
            redeliveryDelayResult = Math.round(backOffMultiplier * previousDelay);
        } else {
            redeliveryDelayResult = previousDelay;
        }

        if (useCollisionAvoidance) {

            /*
             * First random determines +/-, second random determines how far to
             * go in that direction. -cgs
             */
            Random random = getRandomNumberGenerator();
            double variance = (random.nextBoolean() ? collisionAvoidanceFactor : -collisionAvoidanceFactor)
                              * random.nextDouble();
            redeliveryDelayResult += redeliveryDelayResult * variance;
        }

        if (maximumRedeliveryDelay > 0 && redeliveryDelay > maximumRedeliveryDelay) {
            redeliveryDelayResult = maximumRedeliveryDelay;
        }

        return redeliveryDelayResult;
    }

    /**
     * Calculates the delay using the delay pattern
     */
    protected static long calculateRedeliverDelayUsingPattern(String delayPattern, int redeliveryCounter) {
        String[] groups = delayPattern.split(";");
        // find the group where the redelivery counter matches
        long answer = 0;
        for (String group : groups) {
            long delay = Long.valueOf(ObjectHelper.after(group, ":"));
            int count = Integer.valueOf(ObjectHelper.before(group, ":"));
            if (count > redeliveryCounter) {
                break;
            } else {
                answer = delay;
            }
        }

        return answer;
    }


    // Builder methods
    // -------------------------------------------------------------------------

    /**
     * Sets the initial redelivery delay in milliseconds
     *
     * @deprecated use redeliveryDelay instead
     */
    @Deprecated
    public RedeliveryPolicy redeliverDelay(long delay) {
        return redeliveryDelay(delay);
    }

    /**
     * Sets the initial redelivery delay in milliseconds
     */
    public RedeliveryPolicy redeliveryDelay(long delay) {
        setRedeliveryDelay(delay);
        return this;
    }

    /**
     * Sets the maximum number of times a message exchange will be redelivered
     */
    public RedeliveryPolicy maximumRedeliveries(int maximumRedeliveries) {
        setMaximumRedeliveries(maximumRedeliveries);
        return this;
    }

    /**
     * Enables collision avoidance which adds some randomization to the backoff
     * timings to reduce contention probability
     */
    public RedeliveryPolicy useCollisionAvoidance() {
        setUseCollisionAvoidance(true);
        return this;
    }

    /**
     * Enables exponential backoff using the {@link #getBackOffMultiplier()} to
     * increase the time between retries
     */
    public RedeliveryPolicy useExponentialBackOff() {
        setUseExponentialBackOff(true);
        return this;
    }

    /**
     * Enables exponential backoff and sets the multiplier used to increase the
     * delay between redeliveries
     */
    public RedeliveryPolicy backOffMultiplier(double multiplier) {
        useExponentialBackOff();
        setBackOffMultiplier(multiplier);
        return this;
    }

    /**
     * Enables collision avoidance and sets the percentage used
     */
    public RedeliveryPolicy collisionAvoidancePercent(double collisionAvoidancePercent) {
        useCollisionAvoidance();
        setCollisionAvoidancePercent(collisionAvoidancePercent);
        return this;
    }

    /**
     * Sets the maximum redelivery delay if using exponential back off.
     * Use -1 if you wish to have no maximum
     */
    public RedeliveryPolicy maximumRedeliveryDelay(long maximumRedeliveryDelay) {
        setMaximumRedeliveryDelay(maximumRedeliveryDelay);
        return this;
    }

    /**
     * Sets the logging level to use for log messages when retries have been exhausted.
     */
    public RedeliveryPolicy retriesExhaustedLogLevel(LoggingLevel retriesExhaustedLogLevel) {
        setRetriesExhaustedLogLevel(retriesExhaustedLogLevel);
        return this;
    }    

    /**
     * Sets the logging level to use for log messages when retries are attempted.
     */    
    public RedeliveryPolicy retryAttemptedLogLevel(LoggingLevel retryAttemptedLogLevel) {
        setRetryAttemptedLogLevel(retryAttemptedLogLevel);
        return this;
    }

    /**
     * Sets whether to log retry attempts
     */
    public RedeliveryPolicy logRetryAttempted(boolean logRetryAttempted) {
        setLogRetryAttempted(logRetryAttempted);
        return this;
    }

    /**
     * Sets whether to log stacktrace for failed messages.
     */
    public RedeliveryPolicy logStackTrace(boolean logStackTrace) {
        setLogStackTrace(logStackTrace);
        return this;
    }

    /**
     * Sets whether to log stacktrace for failed redelivery attempts
     */
    public RedeliveryPolicy logRetryStackTrace(boolean logRetryStackTrace) {
        setLogRetryStackTrace(logRetryStackTrace);
        return this;
    }

    /**
     * Sets whether to log errors even if its handled
     */
    public RedeliveryPolicy logHandled(boolean logHandled) {
        setLogHandled(logHandled);
        return this;
    }

    /**
     * Sets whether to log exhausted errors
     */
    public RedeliveryPolicy logExhausted(boolean logExhausted) {
        setLogExhausted(logExhausted);
        return this;
    }

    /**
     * Sets the delay pattern with delay intervals.
     */
    public RedeliveryPolicy delayPattern(String delayPattern) {
        setDelayPattern(delayPattern);
        return this;
    }

    /**
     * Disables redelivery by setting maximum redeliveries to 0.
     */
    public RedeliveryPolicy disableRedelivery() {
        setMaximumRedeliveries(0);
        return this;
    }

    // Properties
    // -------------------------------------------------------------------------
    @Deprecated
    public long getRedeliverDelay() {
        return getRedeliveryDelay();
    }
    
    @Deprecated
    public void setRedeliverDelay(long redeliveryDelay) {
        setRedeliveryDelay(redeliveryDelay);
    }
    
    public long getRedeliveryDelay() {
        return redeliveryDelay;
    }

    /**
     * Sets the initial redelivery delay in milliseconds
     */
    public void setRedeliveryDelay(long redeliverDelay) {
        this.redeliveryDelay = redeliverDelay;
        // if max enabled then also set max to this value in case max was too low
        if (maximumRedeliveryDelay > 0 && redeliverDelay > maximumRedeliveryDelay) {
            this.maximumRedeliveryDelay = redeliverDelay;
        }
    }

    public double getBackOffMultiplier() {
        return backOffMultiplier;
    }

    /**
     * Sets the multiplier used to increase the delay between redeliveries if
     * {@link #setUseExponentialBackOff(boolean)} is enabled
     */
    public void setBackOffMultiplier(double backOffMultiplier) {
        this.backOffMultiplier = backOffMultiplier;
    }

    public long getCollisionAvoidancePercent() {
        return Math.round(collisionAvoidanceFactor * 100);
    }

    /**
     * Sets the percentage used for collision avoidance if enabled via
     * {@link #setUseCollisionAvoidance(boolean)}
     */
    public void setCollisionAvoidancePercent(double collisionAvoidancePercent) {
        this.collisionAvoidanceFactor = collisionAvoidancePercent * 0.01d;
    }

    public double getCollisionAvoidanceFactor() {
        return collisionAvoidanceFactor;
    }

    /**
     * Sets the factor used for collision avoidance if enabled via
     * {@link #setUseCollisionAvoidance(boolean)}
     */
    public void setCollisionAvoidanceFactor(double collisionAvoidanceFactor) {
        this.collisionAvoidanceFactor = collisionAvoidanceFactor;
    }

    public int getMaximumRedeliveries() {
        return maximumRedeliveries;
    }

    /**
     * Sets the maximum number of times a message exchange will be redelivered.
     * Setting a negative value will retry forever.
     */
    public void setMaximumRedeliveries(int maximumRedeliveries) {
        this.maximumRedeliveries = maximumRedeliveries;
    }

    public long getMaximumRedeliveryDelay() {
        return maximumRedeliveryDelay;
    }

    /**
     * Sets the maximum redelivery delay.
     * Use -1 if you wish to have no maximum
     */
    public void setMaximumRedeliveryDelay(long maximumRedeliveryDelay) {
        this.maximumRedeliveryDelay = maximumRedeliveryDelay;
    }

    public boolean isUseCollisionAvoidance() {
        return useCollisionAvoidance;
    }

    /**
     * Enables/disables collision avoidance which adds some randomization to the
     * backoff timings to reduce contention probability
     */
    public void setUseCollisionAvoidance(boolean useCollisionAvoidance) {
        this.useCollisionAvoidance = useCollisionAvoidance;
    }

    public boolean isUseExponentialBackOff() {
        return useExponentialBackOff;
    }

    /**
     * Enables/disables exponential backoff using the
     * {@link #getBackOffMultiplier()} to increase the time between retries
     */
    public void setUseExponentialBackOff(boolean useExponentialBackOff) {
        this.useExponentialBackOff = useExponentialBackOff;
    }

    protected static synchronized Random getRandomNumberGenerator() {
        if (randomNumberGenerator == null) {
            randomNumberGenerator = new Random();
        }
        return randomNumberGenerator;
    }

    /**
     * Sets the logging level to use for log messages when retries have been exhausted.
     */    
    public void setRetriesExhaustedLogLevel(LoggingLevel retriesExhaustedLogLevel) {
        this.retriesExhaustedLogLevel = retriesExhaustedLogLevel;        
    }
    
    public LoggingLevel getRetriesExhaustedLogLevel() {
        return retriesExhaustedLogLevel;
    }

    /**
     * Sets the logging level to use for log messages when retries are attempted.
     */    
    public void setRetryAttemptedLogLevel(LoggingLevel retryAttemptedLogLevel) {
        this.retryAttemptedLogLevel = retryAttemptedLogLevel;
    }

    public LoggingLevel getRetryAttemptedLogLevel() {
        return retryAttemptedLogLevel;
    }

    public String getDelayPattern() {
        return delayPattern;
    }

    /**
     * Sets an optional delay pattern to use instead of fixed delay.
     */
    public void setDelayPattern(String delayPattern) {
        this.delayPattern = delayPattern;
    }

    public boolean isLogStackTrace() {
        return logStackTrace;
    }

    /**
     * Sets whether stack traces should be logged or not
     */
    public void setLogStackTrace(boolean logStackTrace) {
        this.logStackTrace = logStackTrace;
    }

    public boolean isLogRetryStackTrace() {
        return logRetryStackTrace;
    }

    /**
     * Sets whether stack traces should be logged or not
     */
    public void setLogRetryStackTrace(boolean logRetryStackTrace) {
        this.logRetryStackTrace = logRetryStackTrace;
    }

    public boolean isLogHandled() {
        return logHandled;
    }

    /**
     * Sets whether errors should be logged even if its handled
     */
    public void setLogHandled(boolean logHandled) {
        this.logHandled = logHandled;
    }

    public boolean isLogContinued() {
        return logContinued;
    }

    /**
     * Sets whether errors should be logged even if its continued
     */
    public void setLogContinued(boolean logContinued) {
        this.logContinued = logContinued;
    }

    public boolean isLogRetryAttempted() {
        return logRetryAttempted;
    }

    /**
     * Sets whether retry attempts should be logged or not
     */
    public void setLogRetryAttempted(boolean logRetryAttempted) {
        this.logRetryAttempted = logRetryAttempted;
    }

    public boolean isLogExhausted() {
        return logExhausted;
    }

    /**
     * Sets whether exhausted exceptions should be logged or not
     */
    public void setLogExhausted(boolean logExhausted) {
        this.logExhausted = logExhausted;
    }
}
