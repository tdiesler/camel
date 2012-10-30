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
package org.apache.camel.component.sjms.taskmanager;

/**
 * A thread safe factory that creates an instance of the TimedTaskManager.
 */
public final class TimedTaskManagerFactory {

    /**
     * Private default constructor for utility class
     */
    private TimedTaskManagerFactory() {
    }

    private static class TimedTaskManagerHolder {
        private static final TimedTaskManager INSTANCE = new TimedTaskManager();
    }

    /**
     * Returns the local instance of the {@link TimedTaskManager}.
     * 
     * @return TimedTaskManager
     */
    public static TimedTaskManager getInstance() {
        return TimedTaskManagerHolder.INSTANCE;
    }
}
