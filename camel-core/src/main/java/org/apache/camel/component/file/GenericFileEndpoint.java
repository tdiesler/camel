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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Component;
import org.apache.camel.Exchange;
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
 * Generic FileEndpoint
 */
public abstract class GenericFileEndpoint<T> extends ScheduledPollEndpoint {

    protected static final transient String DEFAULT_STRATEGYFACTORY_CLASS = "org.apache.camel.component.file.strategy.GenericFileProcessStrategyFactory";
    protected static final transient int DEFAULT_IDEMPOTENT_CACHE_SIZE = 1000;

    protected final transient Log log = LogFactory.getLog(getClass());

    protected GenericFileProcessStrategy<T> processStrategy;
    protected GenericFileOperations<T> operations;
    protected GenericFileConfiguration configuration;

    protected boolean directory = true;
    protected String localWorkDirectory;
    protected boolean autoCreate = true;
    protected int bufferSize = 128 * 1024;
    protected boolean append = true;
    protected boolean noop;
    protected boolean recursive;
    protected boolean delete;
    protected String tempPrefix;
    protected String excludeNamePrefix;
    protected String excludeNamePostfix;
    protected String includeNamePrefix;
    protected String includeNamePostfix;
    protected String regexPattern;
    protected Expression fileExpression;
    protected Expression moveExpression;
    protected Expression preMoveExpression;
    protected boolean idempotent;
    protected IdempotentRepository idempotentRepository;
    protected GenericFileFilter<T> filter;
    protected Comparator<GenericFile<T>> sorter;
    protected Comparator<GenericFileExchange> sortBy;
    protected String readLock = "none";
    protected long readLockTimeout;
    protected GenericFileExclusiveReadLockStrategy exclusiveReadLockStrategy;

    public GenericFileEndpoint() {
    }

    public GenericFileEndpoint(String endpointUri, Component component) {
        super(endpointUri, component);
    }

    public boolean isSingleton() {
        return true;
    }

    public abstract GenericFileConsumer<T> createConsumer(Processor processor) throws Exception;

    public abstract GenericFileProducer<T> createProducer() throws Exception;

    public abstract GenericFileExchange<T> createExchange(GenericFile<T> file);

    public abstract String getScheme();
    
    /**
     * Return the file name that will be auto-generated for the given message if
     * none is provided
     */
    public String getGeneratedFileName(Message message) {
        return UuidGenerator.generateSanitizedId(message.getMessageId());
    }

    public GenericFileProcessStrategy<T> getGenericFileProcessStrategy() {
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
    @SuppressWarnings("unchecked")
    protected GenericFileProcessStrategy<T> createGenericFileStrategy() {
        Class<?> factory = null;
        try {
            FactoryFinder finder = getCamelContext().createFactoryFinder("META-INF/services/org/apache/camel/component/");
            factory = finder.findClass(getScheme(), "strategy.factory.");
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
            return (GenericFileProcessStrategy<T>) ObjectHelper.invokeMethod(factoryMethod, null, getParamsAsMap());
        } catch (NoSuchMethodException e) {
            throw new TypeNotPresentException(factory.getSimpleName() + ".createGenericFileProcessStrategy(GenericFileEndpoint endpoint) method not found", e);
        }
    }

    public void setGenericFileProcessStrategy(GenericFileProcessStrategy<T> genericFileProcessStrategy) {
        this.processStrategy = genericFileProcessStrategy;
    }

    public boolean isNoop() {
        return noop;
    }

    public void setNoop(boolean noop) {
        this.noop = noop;
    }

    public String getExcludeNamePrefix() {
        return excludeNamePrefix;
    }

    public void setExcludeNamePrefix(String excludeNamePrefix) {
        this.excludeNamePrefix = excludeNamePrefix;
    }

    public String getExcludeNamePostfix() {
        return excludeNamePostfix;
    }

    public void setExcludeNamePostfix(String excludeNamePostfix) {
        this.excludeNamePostfix = excludeNamePostfix;
    }

    public String getIncludeNamePrefix() {
        return includeNamePrefix;
    }

    public void setIncludeNamePrefix(String includeNamePrefix) {
        this.includeNamePrefix = includeNamePrefix;
    }

    public String getIncludeNamePostfix() {
        return includeNamePostfix;
    }

    public void setIncludeNamePostfix(String includeNamePostfix) {
        this.includeNamePostfix = includeNamePostfix;
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

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public Expression getMoveExpression() {
        return moveExpression;
    }

    public void setMoveExpression(Expression moveExpression) {
        this.moveExpression = moveExpression;
    }

    /**
     * Sets the move expression based on
     * {@link org.apache.camel.language.simple.FileLanguage}
     */
    public void setMoveExpression(String fileLanguageExpression) {
        this.moveExpression = FileLanguage.file(fileLanguageExpression);
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

    public Expression getFileExpression() {
        return fileExpression;
    }

    public void setFileExpression(Expression fileExpression) {
        this.fileExpression = fileExpression;
    }

    /**
     * Sets the file expression based on
     * {@link org.apache.camel.language.simple.FileLanguage}
     */
    public void setFileExpression(String fileLanguageExpression) {
        this.fileExpression = FileLanguage.file(fileLanguageExpression);
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

    public GenericFileFilter<T> getFilter() {
        return filter;
    }

    public void setFilter(GenericFileFilter<T> filter) {
        this.filter = filter;
    }

    public Comparator<GenericFile<T>> getSorter() {
        return sorter;
    }

    public void setSorter(Comparator<GenericFile<T>> sorter) {
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
        setSortBy(GenericFileDefaultSorter.sortByFileLanguage(expression, reverse));
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
        if (configuration == null) {
            configuration = new GenericFileConfiguration();
        }
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

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public boolean isAutoCreate() {
        return autoCreate;
    }

    public void setAutoCreate(boolean autoCreate) {
        this.autoCreate = autoCreate;
    }

    public GenericFileOperations<T> getOperations() {
        return operations;
    }

    public void setOperations(GenericFileOperations<T> operations) {
        this.operations = operations;
    }

    public GenericFileProcessStrategy<T> getProcessStrategy() {
        return processStrategy;
    }

    public void setProcessStrategy(GenericFileProcessStrategy<T> processStrategy) {
        this.processStrategy = processStrategy;
    }

    public String getLocalWorkDirectory() {
        return localWorkDirectory;
    }

    public void setLocalWorkDirectory(String localWorkDirectory) {
        this.localWorkDirectory = localWorkDirectory;
    }

    /**
     * Configures the given message with the file which sets the body to the
     * file object.
     */
    public void configureMessage(GenericFile<T> file, Message message) {
        message.setBody(file);

        // compute the name that was written, it should be relative to the endpoint configuraion
        String name = file.getRelativeFileName();

        if (isDirectory() && name.startsWith(getConfiguration().getFile())) {
            // remove the file path configured on the endpoint for directory=true
            name = name.substring(getConfiguration().getFile().length());
        } else if (!isDirectory()) {
            // use the filename for directory=false
            name = file.getFileName();
        }

        if (name.startsWith(File.separator) || name.startsWith("/")) {
            // skip trailing /
            name = name.substring(1);
        }

        message.setHeader(Exchange.FILE_NAME, name);
    }

    protected Map<String, Object> getParamsAsMap() {
        Map<String, Object> params = new HashMap<String, Object>();

        if (isNoop()) {
            params.put("noop", Boolean.toString(true));
        }
        if (isDelete()) {
            params.put("delete", Boolean.toString(true));
        }
        if (moveExpression != null) {
            params.put("moveExpression", moveExpression);
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
            params.put("readLockTimeout", readLockTimeout);
        }
        return params;
    }

}
