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
package org.fusesource.camel.management;

import org.apache.camel.CamelContext;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * @version $Revision$
 */
public class SimpleFuseManagementStrategtTest extends CamelTestSupport {

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();
        context.setManagementStrategy(new FuseManagementStrategy());
        return context;
    }

    @Test
    public void testFuseManagementStrategy() throws Exception {
        assertIsInstanceOf(FuseManagementStrategy.class, context.getManagementStrategy());
    }

}
