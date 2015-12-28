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
package org.apache.camel.test.junit.rule.mllp;

/**
 * Base Exception for MLLP JUnit Rules
 */
public class MllpJUnitResourceException extends RuntimeException {
    public MllpJUnitResourceException(String message) {
        super(message);
    }

    public MllpJUnitResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MllpJUnitResourceException(Throwable cause) {
        super(cause);
    }

    public MllpJUnitResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
