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
package org.apache.camel.component.file.strategy;

import java.io.File;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Expression;
import org.apache.camel.component.file.GenericFileExclusiveReadLockStrategy;
import org.apache.camel.component.file.GenericFileProcessStrategy;
import org.apache.camel.spi.Language;
import org.apache.camel.util.ObjectHelper;

public final class FileProcessStrategyFactory {

    private FileProcessStrategyFactory() {
    }

    public static GenericFileProcessStrategy<File> createGenericFileProcessStrategy(CamelContext context, Map<String, Object> params) {

        // We assume a value is present only if its value not null for String and 'true' for boolean
        Expression moveExpression = (Expression) params.get("move");
        Expression moveFailedExpression = (Expression) params.get("moveFailed");
        Expression preMoveExpression = (Expression) params.get("preMove");
        boolean isNoop = params.get("noop") != null;
        boolean isDelete = params.get("delete") != null;
        boolean isMove = moveExpression != null || preMoveExpression != null || moveFailedExpression != null;

        if (isNoop) {
            GenericFileNoOpProcessStrategy<File> strategy = new GenericFileNoOpProcessStrategy<File>();
            strategy.setExclusiveReadLockStrategy(getExclusiveReadLockStrategy(params));
            return strategy;
        } else if (isDelete) {
            GenericFileDeleteProcessStrategy<File> strategy = new GenericFileDeleteProcessStrategy<File>();
            strategy.setExclusiveReadLockStrategy(getExclusiveReadLockStrategy(params));
            return strategy;
        } else if (isMove) {
            GenericFileRenameProcessStrategy<File> strategy = new GenericFileRenameProcessStrategy<File>();
            strategy.setExclusiveReadLockStrategy(getExclusiveReadLockStrategy(params));
            if (moveExpression != null) {
                GenericFileExpressionRenamer<File> renamer = new GenericFileExpressionRenamer<File>();
                renamer.setExpression(moveExpression);
                strategy.setCommitRenamer(renamer);
            } else {
                strategy.setCommitRenamer(getDefaultCommitRenamer(context));
            }
            if (preMoveExpression != null) {
                GenericFileExpressionRenamer<File> renamer = new GenericFileExpressionRenamer<File>();
                renamer.setExpression(preMoveExpression);
                strategy.setBeginRenamer(renamer);
            }
            if (moveFailedExpression != null) {
                GenericFileExpressionRenamer<File> renamer = new GenericFileExpressionRenamer<File>();
                renamer.setExpression(moveFailedExpression);
                strategy.setFailureRenamer(renamer);
            }
            return strategy;
        } else {
            // default strategy will move files in a .camel/ subfolder where the file was consumed
            GenericFileRenameProcessStrategy<File> strategy = new GenericFileRenameProcessStrategy<File>();
            strategy.setExclusiveReadLockStrategy(getExclusiveReadLockStrategy(params));
            strategy.setCommitRenamer(getDefaultCommitRenamer(context));
            return strategy;
        }
    }

    private static GenericFileExpressionRenamer<File> getDefaultCommitRenamer(CamelContext context) {
        // use context to lookup language to let it be loose coupled
        Language language = context.resolveLanguage("file");
        Expression expression = language.createExpression("${file:parent}/.camel/${file:onlyname}");
        return new GenericFileExpressionRenamer<File>(expression);
    }

    @SuppressWarnings("unchecked")
    private static GenericFileExclusiveReadLockStrategy<File> getExclusiveReadLockStrategy(Map<String, Object> params) {
        GenericFileExclusiveReadLockStrategy strategy = (GenericFileExclusiveReadLockStrategy) params.get("exclusiveReadLockStrategy");
        if (strategy != null) {
            return strategy;
        }

        // no explicit stategy set then fallback to readLock option
        String readLock = (String) params.get("readLock");
        if (ObjectHelper.isNotEmpty(readLock)) {
            if ("none".equals(readLock) || "false".equals(readLock)) {
                return null;
            } else if ("fileLock".equals(readLock)) {
                GenericFileExclusiveReadLockStrategy<File> readLockStrategy = new FileLockExclusiveReadLockStrategy();
                Long timeout = (Long) params.get("readLockTimeout");
                if (timeout != null) {
                    readLockStrategy.setTimeout(timeout);
                }
                return readLockStrategy;
            } else if ("rename".equals(readLock)) {
                GenericFileExclusiveReadLockStrategy<File> readLockStrategy = new GenericFileRenameExclusiveReadLockStrategy<File>();
                Long timeout = (Long) params.get("readLockTimeout");
                if (timeout != null) {
                    readLockStrategy.setTimeout(timeout);
                }
                return readLockStrategy;
            } else if ("changed".equals(readLock)) {
                GenericFileExclusiveReadLockStrategy readLockStrategy = new FileChangedExclusiveReadLockStrategy();
                Long timeout = (Long) params.get("readLockTimeout");
                if (timeout != null) {
                    readLockStrategy.setTimeout(timeout);
                }
                return readLockStrategy;
            } else if ("markerFile".equals(readLock)) {
                return new MarkerFileExclusiveReadLockStrategy();
            }
        }

        return null;
    }
}
