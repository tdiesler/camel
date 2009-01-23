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
package org.apache.camel.component.file;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Expression;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.camel.language.simple.FileLanguage;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.util.FactoryFinder;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.UuidGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generic Endpoint
 */
public abstract class GenericFileEndpoint extends ScheduledPollEndpoint {

    protected static final transient String DEFAULT_STRATEGYFACTORY_CLASS = "org.apache.camel.component.file.strategy.GenericFileProcessStrategyFactory";
    protected static final transient int DEFAULT_IDEMPOTENT_CACHE_SIZE = 1000;

    protected final transient Log log = LogFactory.getLog(getClass());

    protected GenericFileProcessStrategy processStrategy;
    protected GenericFileOperations operations;
    protected GenericFileConfiguration configuration;

    protected boolean noop;
    protected String tempPrefix;
    protected String moveNamePrefix;
    protected String moveNamePostfix;
    protected String preMoveNamePrefix;
    protected String preMoveNamePostfix;
    protected String excludedNamePrefix;
    protected String excludedNamePostfix;
    protected boolean recursive;
    protected String regexPattern;
    protected boolean setNames = true;
    protected boolean delete;
    protected Expression expression;
    protected Expression preMoveExpression;
    protected boolean idempotent;
    protected IdempotentRepository idempotentRepository;
    protected GenericFileFilter filter;
    protected Comparator<GenericFile> sorter;
    protected Comparator<GenericFileExchange> sortBy;
    protected String readLock = "none";
    protected long readLockTimeout;
    protected GenericFileExclusiveReadLockStrategy exclusiveReadLockStrategy;

    public GenericFileEndpoint(String endpointUri, Component component) {
        super(endpointUri, component);
    }

    public GenericFileEndpoint(String endpointUri, CamelContext context) {
        super(endpointUri, context);
    }

    public GenericFileEndpoint(String endpointUri) {
        super(endpointUri);
    }

    public GenericFileEndpoint() {
        super();
    }

    public boolean isSingleton() {
        return true;
    }

    public abstract GenericFileConsumer createConsumer(Processor processor) throws Exception;

    public abstract GenericFileProducer createProducer() throws Exception;

    public abstract GenericFileExchange createExchange(GenericFile file);

    public abstract GenericFileExchange createExchange();

    /**
     * Returns the first portion of the "protocol" in use. We use this so we can
     * look back into the META-INF protocol file to locate the default strategy
     */
    protected abstract String getUriProtocol();

    /**
     * Return the file name that will be auto-generated for the given message if
     * none is provided
     */
    public String getGeneratedFileName(Message message) {
        return getFileFriendlyMessageId(message.getMessageId());
    }

    protected String getFileFriendlyMessageId(String id) {
        return UuidGenerator.generateSanitizedId(id);
    }

    public GenericFileProcessStrategy getGenericFileProcessStrategy() {
        if (processStrategy == null) {
            processStrategy = createGenericFileStrategy();
            if (log.isDebugEnabled()) {
                log.debug("Using Generic file process strategy: " + processStrategy);
            }
        }
        return processStrategy;
    }

    /**
     * A strategy method to lazily create the file strategy
     */
    protected GenericFileProcessStrategy createGenericFileStrategy() {
        Class<?> factory = null;
        try {
            FactoryFinder finder = getCamelContext().createFactoryFinder("META-INF/services/org/apache/camel/component/");
            factory = finder.findClass("ftp", "strategy.factory.");
        } catch (ClassNotFoundException e) {
            log.debug("'strategy.factory.class' not found", e);
        } catch (IOException e) {
            log.debug("No strategy factory defined in 'META-INF/services/org/apache/camel/component/'", e);
        }

        if (factory == null) {
            // use default
            factory = ObjectHelper.loadClass(DEFAULT_STRATEGYFACTORY_CLASS);
            if (factory == null) {
                throw new TypeNotPresentException(DEFAULT_STRATEGYFACTORY_CLASS + " class not found", null);
            }
        }

        try {
            Method factoryMethod = factory.getMethod("createGenericFileProcessStrategy", Map.class);
            return (GenericFileProcessStrategy) ObjectHelper.invokeMethod(factoryMethod, null, getParamsAsMap());
        } catch (NoSuchMethodException e) {
            throw new TypeNotPresentException(factory.getSimpleName() + ".createGenericFileProcessStrategy(GenericFileEndpoint endpoint) method not found", e);
        }
    }

    public void setGenericFileProcessStrategy(GenericFileProcessStrategy genericFileProcessStrategy) {
        this.processStrategy = genericFileProcessStrategy;
    }

    public boolean isNoop() {
        return noop;
    }

    public void setNoop(boolean noop) {
        this.noop = noop;
    }

    public String getMoveNamePrefix() {
        return moveNamePrefix;
    }

    public void setMoveNamePrefix(String moveNamePrefix) {
        this.moveNamePrefix = moveNamePrefix;
    }

    public String getMoveNamePostfix() {
        return moveNamePostfix;
    }

    public void setMoveNamePostfix(String moveNamePostfix) {
        this.moveNamePostfix = moveNamePostfix;
    }

    public String getPreMoveNamePrefix() {
        return preMoveNamePrefix;
    }

    public void setPreMoveNamePrefix(String preMoveNamePrefix) {
        this.preMoveNamePrefix = preMoveNamePrefix;
    }

    public String getPreMoveNamePostfix() {
        return preMoveNamePostfix;
    }

    public void setPreMoveNamePostfix(String preMoveNamePostfix) {
        this.preMoveNamePostfix = preMoveNamePostfix;
    }

    public String getExcludedNamePrefix() {
        return excludedNamePrefix;
    }

    public void setExcludedNamePrefix(String excludedNamePrefix) {
        this.excludedNamePrefix = excludedNamePrefix;
    }

    public String getExcludedNamePostfix() {
        return excludedNamePostfix;
    }

    public void setExcludedNamePostfix(String excludedNamePostfix) {
        this.excludedNamePostfix = excludedNamePostfix;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public String getRegexPattern() {
        return regexPattern;
    }

    public void setRegexPattern(String regexPattern) {
        this.regexPattern = regexPattern;
    }

    public boolean isSetNames() {
        return setNames;
    }

    public void setSetNames(boolean setNames) {
        this.setNames = setNames;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * Sets the expression based on
     * {@link org.apache.camel.language.simple.FileLanguage}
     */
    public void setExpression(String fileLanguageExpression) {
        this.expression = FileLanguage.file(fileLanguageExpression);
    }

    public Expression getPreMoveExpression() {
        return preMoveExpression;
    }

    public void setPreMoveExpression(Expression preMoveExpression) {
        this.preMoveExpression = preMoveExpression;
    }

    /**
     * Sets the pre move expression based on
     * {@link org.apache.camel.language.simple.FileLanguage}
     */
    public void setPreMoveExpression(String fileLanguageExpression) {
        this.preMoveExpression = FileLanguage.file(fileLanguageExpression);
    }

    public boolean isIdempotent() {
        return idempotent;
    }

    public void setIdempotent(boolean idempotent) {
        this.idempotent = idempotent;
    }

    public IdempotentRepository getIdempotentRepository() {
        return idempotentRepository;
    }

    public void setIdempotentRepository(IdempotentRepository idempotentRepository) {
        this.idempotentRepository = idempotentRepository;
    }

    public GenericFileFilter getFilter() {
        return filter;
    }

    public void setFilter(GenericFileFilter filter) {
        this.filter = filter;
    }

    public Comparator<GenericFile> getSorter() {
        return sorter;
    }

    public void setSorter(Comparator<GenericFile> sorter) {
        this.sorter = sorter;
    }

    public Comparator<GenericFileExchange> getSortBy() {
        return sortBy;
    }

    public void setSortBy(Comparator<GenericFileExchange> sortBy) {
        this.sortBy = sortBy;
    }

    public void setSortBy(String expression) {
        setSortBy(expression, false);
    }

    public void setSortBy(String expression, boolean reverse) {
        setSortBy(GenericDefaultFileSorter.sortByFileLanguage(expression, reverse));
    }

    public String getTempPrefix() {
        return tempPrefix;
    }

    /**
     * Enables and uses temporary prefix when writing files, after write it will
     * be renamed to the correct name.
     */
    public void setTempPrefix(String tempPrefix) {
        this.tempPrefix = tempPrefix;
    }

    public GenericFileConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(GenericFileConfiguration configuration) {
        this.configuration = configuration;
    }

    public GenericFileExclusiveReadLockStrategy getExclusiveReadLockStrategy() {
        return exclusiveReadLockStrategy;
    }

    public void setExclusiveReadLockStrategy(GenericFileExclusiveReadLockStrategy exclusiveReadLockStrategy) {
        this.exclusiveReadLockStrategy = exclusiveReadLockStrategy;
    }

    public String getReadLock() {
        return readLock;
    }

    public void setReadLock(String readLock) {
        this.readLock = readLock;
    }

    public long getReadLockTimeout() {
        return readLockTimeout;
    }

    public void setReadLockTimeout(long readLockTimeout) {
        this.readLockTimeout = readLockTimeout;
    }

    /**
     * Should the file be moved after consuming?
     */
    public boolean isMoveFile() {
        return moveNamePostfix != null || moveNamePrefix != null
                || preMoveNamePostfix != null || preMoveNamePrefix != null
                || expression != null;
    }

    /**
     * Configures the given message with the file which sets the body to the
     * file object and sets the {@link FileComponent#HEADER_FILE_NAME} header.
     */
    public void configureMessage(GenericFile file, Message message) {
        message.setBody(file);
        message.setHeader(FileComponent.HEADER_FILE_NAME, file.getRelativeFileName());
    }

    protected Map<String, Object> getParamsAsMap() {
        Map<String, Object> params = new HashMap<String, Object>();

        if (isNoop()) {
            params.put("noop", Boolean.toString(true));
        }
        if (isDelete()) {
            params.put("delete", Boolean.toString(true));
        }
        if (moveNamePrefix != null) {
            params.put("moveNamePrefix", moveNamePrefix);
        }
        if (moveNamePostfix != null) {
            params.put("moveNamePostfix", moveNamePostfix);
        }
        if (preMoveNamePrefix != null) {
            params.put("preMoveNamePrefix", preMoveNamePrefix);
        }
        if (preMoveNamePostfix != null) {
            params.put("preMoveNamePostfix", preMoveNamePostfix);
        }
        if (expression != null) {
            params.put("expression", expression);
        }
        if (preMoveExpression != null) {
            params.put("preMoveExpression", preMoveExpression);
        }
        if (exclusiveReadLockStrategy != null) {
            params.put("exclusiveReadLockStrategy", exclusiveReadLockStrategy);
        }
        if (readLock != null) {
            params.put("readLock", readLock);
        }
        if (readLockTimeout > 0) {
            params.put("readLockTimeout", Long.valueOf(readLockTimeout));
        }
        return params;
    }

}
