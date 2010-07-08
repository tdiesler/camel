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
package org.apache.camel;

import java.lang.management.ManagementFactory;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.management.DefaultManagementAgent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class TestSupportJmxCleanup {
    private static final transient Log LOG = LogFactory.getLog(TestSupportJmxCleanup.class);

    private TestSupportJmxCleanup() {
        // no instances
    }

    public static void removeMBeans(String domain) throws Exception {
        MBeanServer mbsc =  ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> s = mbsc.queryNames(new ObjectName(getDomainName(domain) + ":*"), null);
        for (ObjectName on : s) {
            mbsc.unregisterMBean(on);
        }
    }

    // useful helper to invoke in TestSupport to figure out what test leave junk behind
    public static void traceMBeans(String domain) throws Exception {
        MBeanServer mbsc =  ManagementFactory.getPlatformMBeanServer();
        String d = getDomainName(domain);
        Set<ObjectName> s = mbsc.queryNames(new ObjectName(d + ":*"), null);
        if (s.size() > 0) {
            LOG.warn(" + " + s.size() + " ObjectNames registered in domain \"" + d + "\"");
            for (ObjectName on : s) {
                LOG.warn(" |  " + on);
            }
        }
    }

    private static String getDomainName(String domain) {
        return domain == null ? DefaultManagementAgent.DEFAULT_DOMAIN : domain;
    }

}
