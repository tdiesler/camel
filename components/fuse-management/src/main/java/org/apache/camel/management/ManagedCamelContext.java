/*
 * © 2001-2009, Progress Software Corporation and/or its subsidiaries or affiliates.  All rights reserved.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.apache.camel.management;

import java.io.IOException;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.ServiceSupport;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * @version $Revision$
 */
@ManagedResource(description = "Managed CamelContext", currencyTimeLimit = 15)
@Deprecated
public class ManagedCamelContext {

    private CamelContext context;

    public ManagedCamelContext(CamelContext context) {
        this.context = context;
    }

    public CamelContext getContext() {
        return context;
    }

    @ManagedAttribute(description = "Name")
    public String getName() throws IOException {
        return context.getName();
    }

    @ManagedAttribute(description = "Camel running state")
    public boolean isStarted() throws IOException {
        return ((ServiceSupport) context).isStarted();
    }

    @ManagedOperation(description = "Start Camel")
    public void start() throws IOException {
        try {
            context.start();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    @ManagedOperation(description = "Stop Camel")
    public void stop() throws IOException {
        try {
            context.stop();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

}
