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
package org.apache.camel.management;

import javax.management.ObjectName;

import org.fusesource.commons.management.basic.AbstractManagementStrategy;

/**
 * @version $Revision$
 */
public class CamelManagementStrategy extends AbstractManagementStrategy {

    private CamelNamingStrategy naming = new CamelNamingStrategy();

    public void addManagedObject(Object managedObject) throws Exception {
        ObjectName objectName = null;

        if (managedObject instanceof ManagedEndpoint) {
            ManagedEndpoint me = (ManagedEndpoint) managedObject;
            objectName = naming.getObjectName(me);
        }

        if (objectName != null) {
            registerMBean(managedObject, objectName);
        }
    }

    public void removeManagedObject(Object o) throws Exception {
    }

    private void registerMBean(Object managedObject, ObjectName objectName) throws Exception {
        super.getMbeanServer().registerMBean(managedObject, objectName);
    }


}
