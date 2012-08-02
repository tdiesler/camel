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
package org.apache.camel.component.sjms;

import org.apache.camel.RuntimeCamelException;

/**
 * TODO Add Class documentation for MissingHeaderException
 *
 * @author sully6768
 */
public class MissingHeaderException extends RuntimeCamelException {

    /**
     * 
     */
    private static final long serialVersionUID = -6184009502090347023L;

    /**
     * TODO Add Constructor Javadoc
     *
     */
    public MissingHeaderException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * TODO Add Constructor Javadoc
     *
     * @param message
     * @param cause
     */
    public MissingHeaderException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * TODO Add Constructor Javadoc
     *
     * @param message
     */
    public MissingHeaderException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * TODO Add Constructor Javadoc
     *
     * @param cause
     */
    public MissingHeaderException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    
}
