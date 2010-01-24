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

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.camel.processor.idempotent.MemoryIdempotentRepository;
import org.apache.camel.spi.FactoryFinder;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.spi.Language;
import org.apache.camel.util.FileUtil;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.StringHelper;
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
    protected GenericFileConfiguration configuration;

    protected IdempotentRepository<String> inProgressRepository = new MemoryIdempotentRepository();
    protected String localWorkDirectory;
    protected boolean autoCreate = true;
    protected int bufferSize = 128 * 1024;
    protected GenericFileExist fileExist = GenericFileExist.Override;
    protected boolean noop;
    protected boolean recursive;
    protected boolean delete;
    protected boolean flatten;
    protected int maxMessagesPerPoll;
    protected String tempPrefix;
    protected Expression tempFileName;
    protected String include;
    protected String exclude;
    protected Expression fileName;
    protected Expression move;
    protected Expression moveFailed;
    protected Expression preMove;
    protected Boolean idempotent;
    protected IdempotentRepository<String> idempotentRepository;
    protected GenericFileFilter<T> filter;
    protected Comparator<GenericFile<T>> sorter;
    protected Comparator<Exchange> sortBy;
    protected String readLock = "none";
    protected long readLockTimeout = 10000;
    protected GenericFileExclusiveReadLockStrategy<T> exclusiveReadLockStrategy;
    protected boolean keepLastModified;

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

    public abstract Exchange createExchange(GenericFile<T> file);

    public abstract String getScheme();
    
    public abstract char getFileSeparator();
    
    public abstract boolean isAbsolute(String name);
    
    /**
     * Return the file name that will be auto-generated for the given message if
     * none is provided
     */
    public String getGeneratedFileName(Message message) {
        return StringHelper.sanitize(message.getMessageId());
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
            FactoryFinder finder = getCamelContext().getFactoryFinder("META-INF/services/org/apache/camel/component/");
            factory = finder.findClass(getScheme(), "strategy.factory.");
        } catch (ClassNotFoundException e) {
            log.debug("'strategy.factory.class' not found", e);
        } catch (IOException e) {
            log.debug("No strategy factory defined in 'META-INF/services/org/apache/camel/component/'", e);
        }

        if (factory == null) {
            // use default
            factory = this.getCamelContext().getClassResolver().resolveClass(DEFAULT_STRATEGYFACTORY_CLASS);
            if (factory == null) {
                throw new TypeNotPresentException(DEFAULT_STRATEGYFACTORY_CLASS + " class not found", null);
            }
        }

        try {
            Method factoryMethod = factory.getMethod("createGenericFileProcessStrategy", CamelContext.class, Map.class);
            return (GenericFileProcessStrategy<T>) ObjectHelper.invokeMethod(factoryMethod, null, getCamelContext(), getParamsAsMap());
        } catch (NoSuchMethodException e) {
            throw new TypeNotPresentException(factory.getSimpleName() + ".createGenericFileProcessStrategy method not found", e);
        }
    }

    public boolean isNoop() {
        return noop;
    }

    public void setNoop(boolean noop) {
        this.noop = noop;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public String getInclude() {
        return include;
    }

    public void setInclude(String include) {
        this.include = include;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public boolean isFlatten() {
        return flatten;
    }

    public void setFlatten(boolean flatten) {
        this.flatten = flatten;
    }

    public Expression getMove() {
        return move;
    }

    public void setMove(Expression move) {
        this.move = move;
    }

    /**
     * Sets the move failure expression based on
     * {@link org.apache.camel.language.simple.FileLanguage}
     */
    public void setMoveFailed(String fileLanguageExpression) {
        String expression = configureMoveOrPreMoveExpression(fileLanguageExpression);
        this.moveFailed = createFileLanguageExpression(expression);
    }

    public Expression getMoveFailed() {
        return moveFailed;
    }

    public void setMoveFailed(Expression moveFailed) {
        this.moveFailed = moveFailed;
    }

    /**
     * Sets the move expression based on
     * {@link org.apache.camel.language.simple.FileLanguage}
     */
    public void setMove(String fileLanguageExpression) {
        String expression = configureMoveOrPreMoveExpression(fileLanguageExpression);
        this.move = createFileLanguageExpression(expression);
    }

    public Expression getPreMove() {
        return preMove;
    }

    public void setPreMove(Expression preMove) {
        this.preMove = preMove;
    }

    /**
     * Sets the pre move expression based on
     * {@link org.apache.camel.language.simple.FileLanguage}
     */
    public void setPreMove(String fileLanguageExpression) {
        String expression = configureMoveOrPreMoveExpression(fileLanguageExpression);
        this.preMove = createFileLanguageExpression(expression);
    }

    public Expression getFileName() {
        return fileName;
    }

    public void setFileName(Expression fileName) {
        this.fileName = fileName;
    }

    /**
     * Sets the file expression based on
     * {@link org.apache.camel.language.simple.FileLanguage}
     */
    public void setFileName(String fileLanguageExpression) {
        this.fileName = createFileLanguageExpression(fileLanguageExpression);
    }

    public Boolean isIdempotent() {
        return idempotent != null ? idempotent : false;
    }

    boolean isIdempotentSet() {
        return idempotent != null;
    }

    public void setIdempotent(Boolean idempotent) {
        this.idempotent = idempotent;
    }

    public IdempotentRepository<String> getIdempotentRepository() {
        return idempotentRepository;
    }

    public void setIdempotentRepository(IdempotentRepository<String> idempotentRepository) {
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

    public Comparator<Exchange> getSortBy() {
        return sortBy;
    }

    public void setSortBy(Comparator<Exchange> sortBy) {
        this.sortBy = sortBy;
    }

    public void setSortBy(String expression) {
        setSortBy(expression, false);
    }

    public void setSortBy(String expression, boolean reverse) {
        setSortBy(GenericFileDefaultSorter.sortByFileLanguage(getCamelContext(), expression, reverse));
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
        // use only name as we set a prefix in from on the name
        setTempFileName(tempPrefix + "${file:onlyname}");
    }

    public Expression getTempFileName() {
        return tempFileName;
    }

    public void setTempFileName(Expression tempFileName) {
        this.tempFileName = tempFileName;
    }

    public void setTempFileName(String tempFileNameExpression) {
        this.tempFileName = createFileLanguageExpression(tempFileNameExpression);
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

    public GenericFileExclusiveReadLockStrategy<T> getExclusiveReadLockStrategy() {
        return exclusiveReadLockStrategy;
    }

    public void setExclusiveReadLockStrategy(GenericFileExclusiveReadLockStrategy<T> exclusiveReadLockStrategy) {
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
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("BufferSize must be a positive value, was " + bufferSize);
        }
        this.bufferSize = bufferSize;
    }

    public GenericFileExist getFileExist() {
        return fileExist;
    }

    public void setFileExist(GenericFileExist fileExist) {
        this.fileExist = fileExist;
    }

    public boolean isAutoCreate() {
        return autoCreate;
    }

    public void setAutoCreate(boolean autoCreate) {
        this.autoCreate = autoCreate;
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

    public int getMaxMessagesPerPoll() {
        return maxMessagesPerPoll;
    }

    public void setMaxMessagesPerPoll(int maxMessagesPerPoll) {
        this.maxMessagesPerPoll = maxMessagesPerPoll;
    }

    public IdempotentRepository<String> getInProgressRepository() {
        return inProgressRepository;
    }

    public void setInProgressRepository(IdempotentRepository<String> inProgressRepository) {
        this.inProgressRepository = inProgressRepository;
    }

    public boolean isKeepLastModified() {
        return keepLastModified;
    }

    public void setKeepLastModified(boolean keepLastModified) {
        this.keepLastModified = keepLastModified;
    }

    /**
     * Configures the given message with the file which sets the body to the
     * file object.
     */
    public void configureMessage(GenericFile<T> file, Message message) {
        message.setBody(file);

        if (flatten) {
            // when flatten the file name should not contain any paths
            message.setHeader(Exchange.FILE_NAME, file.getFileNameOnly());
        } else {
            // compute name to set on header that should be relative to starting directory
            String name = file.isAbsolute() ? file.getAbsoluteFilePath() : file.getRelativeFilePath();

            // skip leading endpoint configured directory
            String endpointPath = getConfiguration().getDirectory();
            if (ObjectHelper.isNotEmpty(endpointPath) && name.startsWith(endpointPath)) {
                name = ObjectHelper.after(name, getConfiguration().getDirectory() + File.separator);
            }

            // adjust filename
            message.setHeader(Exchange.FILE_NAME, name);
        }
    }

    /**
     * Strategy to configure the move or premove option based on a String input.
     * <p/>
     * @param expression the original string input
     * @return configured string or the original if no modifications is needed
     */
    protected String configureMoveOrPreMoveExpression(String expression) {
        // if the expression already have ${ } placeholders then pass it unmodified
        if (expression.indexOf("${") != -1) {
            return expression;
        }

        // remove trailing slash
        expression = FileUtil.stripTrailingSeparator(expression);

        StringBuilder sb = new StringBuilder();

        // if relative then insert start with the parent folder
        if (!isAbsolute(expression)) {
            sb.append("${file:parent}");
            sb.append(getFileSeparator());
        }
        // insert the directory the end user provided
        sb.append(expression);
        // append only the filename (file:name can contain a relative path, so we must use onlyname)
        sb.append(getFileSeparator());
        sb.append("${file:onlyname}");

        return sb.toString();
    }

    protected Map<String, Object> getParamsAsMap() {
        Map<String, Object> params = new HashMap<String, Object>();

        if (isNoop()) {
            params.put("noop", Boolean.toString(true));
        }
        if (isDelete()) {
            params.put("delete", Boolean.toString(true));
        }
        if (move != null) {
            params.put("move", move);
        }
        if (moveFailed != null) {
            params.put("moveFailed", moveFailed);
        }
        if (preMove != null) {
            params.put("preMove", preMove);
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

    private Expression createFileLanguageExpression(String expression) {
        Language language;
        // only use file language if the name is complex (eg. using $)
        if (expression.contains("$")) {
            language = getCamelContext().resolveLanguage("file");
        } else {
            language = getCamelContext().resolveLanguage("constant");
        }
        return language.createExpression(expression);
    }
}
