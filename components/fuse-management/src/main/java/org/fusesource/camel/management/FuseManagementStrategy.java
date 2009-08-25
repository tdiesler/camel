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

import java.util.EventObject;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.EventFactory;
import org.apache.camel.spi.EventNotifier;
import org.apache.camel.spi.ManagementAgent;
import org.apache.camel.spi.ManagementNamingStrategy;
import org.apache.camel.spi.ManagementStrategy;
import org.fusesource.commons.management.Statistic;

/**
 * FUSE management strategy.
 *
 * @see org.apache.camel.management.DefaultManagementStrategy
 * @see org.apache.camel.management.ManagedManagementStrategy
 * @version $Revision$
 */
public class FuseManagementStrategy implements ManagementStrategy, EventFactory, EventNotifier {

    // TODO: implement me

    public EventNotifier getEventNotifier() {
        return null;
    }

    public void setEventNotifier(EventNotifier eventNotifier) {
    }

    public EventFactory getEventFactory() {
        return null;
    }

    public void setEventFactory(EventFactory eventFactory) {
    }

    public ManagementNamingStrategy getManagementNamingStrategy() {
        return null;
    }

    public void setManagementNamingStrategy(ManagementNamingStrategy managementNamingStrategy) {
    }

    public ManagementAgent getManagementAgent() {
        return null;
    }

    public void setManagementAgent(ManagementAgent managementAgent) {
    }

    public boolean manageProcessor(ProcessorDefinition processorDefinition) {
        return false;
    }

    public void onlyManageProcessorWithCustomId(boolean b) {
    }

    public boolean isOnlyManageProcessorWithCustomId() {
        return false;
    }

    public void manageObject(Object o) throws Exception {
    }

    public void manageNamedObject(Object o, Object o1) throws Exception {
    }

    public <T> T getManagedObjectName(Object o, String s, Class<T> tClass) throws Exception {
        return null;
    }

    public void unmanageObject(Object o) throws Exception {
    }

    public void unmanageNamedObject(Object o) throws Exception {
    }

    public boolean isManaged(Object o, Object o1) {
        return false;
    }

    public void notify(EventObject eventObject) throws Exception {
    }

    public boolean isEnabled(EventObject eventObject) {
        return false;
    }

    public Statistic createStatistic(String s, Object o, Statistic.UpdateMode updateMode) {
        return null;
    }

    public void start() throws Exception {
    }

    public void stop() throws Exception {
    }

    public EventObject createCamelContextStartingEvent(CamelContext camelContext) {
        return null;
    }

    public EventObject createCamelContextStartedEvent(CamelContext camelContext) {
        return null;
    }

    public EventObject createCamelContextStoppingEvent(CamelContext camelContext) {
        return null;
    }

    public EventObject createCamelContextStoppedEvent(CamelContext camelContext) {
        return null;
    }

    public EventObject createRouteStartEvent(Route route) {
        return null;
    }

    public EventObject createRouteStopEvent(Route route) {
        return null;
    }

    public EventObject createExchangeCreatedEvent(Exchange exchange) {
        return null;
    }

    public EventObject createExchangeCompletedEvent(Exchange exchange) {
        return null;
    }

    public EventObject createExchangeFailedEvent(Exchange exchange) {
        return null;
    }
}
