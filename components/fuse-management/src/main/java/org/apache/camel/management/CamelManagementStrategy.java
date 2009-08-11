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

import javax.management.JMException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.commons.management.basic.AbstractManagementStrategy;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;

/**
 * @version $Revision$
 */
public class CamelManagementStrategy extends AbstractManagementStrategy {

    private static final Log LOG = LogFactory.getLog(CamelManagementStrategy.class);

    private final CamelNamingStrategy naming = new CamelNamingStrategy();
    private final MetadataMBeanInfoAssembler assembler;

    public CamelManagementStrategy() {
        assembler = new MetadataMBeanInfoAssembler();
        assembler.setAttributeSource(new AnnotationJmxAttributeSource());
    }

    public void addManagedObject(Object managedObject) throws Exception {
        ObjectName objectName = null;

        if (managedObject instanceof ManagedEndpoint) {
            ManagedEndpoint me = (ManagedEndpoint) managedObject;
            objectName = naming.getObjectName(me);
        }

        if (managedObject instanceof ManagedRoute) {
            ManagedRoute mr = (ManagedRoute) managedObject;
            objectName = naming.getObjectName(mr);
        }

        if (objectName != null) {
            registerMBean(managedObject, objectName);
        }
    }

    public void removeManagedObject(Object o) throws Exception {
    }

    private void registerMBean(Object managedObject, ObjectName objectName) throws Exception {
        try {
            super.getMbeanServer().registerMBean(managedObject, objectName);
        } catch (NotCompliantMBeanException e) {
            // If this is not a "normal" MBean, then try to deploy it using JMX annotations
            // TODO: Consider porting this to commons management so everyone easily can register mbeans without
            // all the JMX hazzle as you just use spring annotations on your managed object
            // the code is from Camel 1.x developed by Willem Tam from the Sonic Team
            ModelMBeanInfo mbi = assembler.getMBeanInfo(managedObject, objectName.toString());
            RequiredModelMBean mbean = (RequiredModelMBean) getMbeanServer().instantiate(RequiredModelMBean.class.getName());
            mbean.setModelMBeanInfo(mbi);
            try {
                mbean.setManagedResource(managedObject, "ObjectReference");
            } catch (InvalidTargetObjectTypeException itote) {
                throw new JMException(itote.getMessage());
            }
            super.getMbeanServer().registerMBean(mbean, objectName);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Registered MBean: " + managedObject + " with name: " + objectName);
        }
    }


}
