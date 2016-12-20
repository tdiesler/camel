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
package org.apache.camel.component.salesforce.api.dto.composite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import org.apache.camel.component.salesforce.api.dto.AbstractDescribedSObjectBase;
import org.apache.camel.component.salesforce.api.dto.AbstractSObjectBase;
import org.apache.camel.component.salesforce.api.dto.RestError;
import org.apache.camel.component.salesforce.api.dto.SObjectDescription;
import org.apache.camel.util.ObjectHelper;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonUnwrapped;

/**
 * Represents one node in the SObject tree request. SObject trees ({@link SObjectTree}) are composed from instances of
 * {@link SObjectNode}s. Each {@link SObjectNode} contains {@link Attributes}, the SObject ({@link AbstractSObjectBase})
 * and any child records linked to it. SObjects at root level are added to {@link SObjectTree} using
 * {@link SObjectTree#addObject(AbstractSObjectBase)}, then you can add child records on the {@link SObjectNode}
 * returned by using {@link #addChild(AbstractDescribedSObjectBase)},
 * {@link #addChildren(AbstractDescribedSObjectBase, AbstractDescribedSObjectBase...)} or
 * {@link #addChild(String, AbstractSObjectBase)} and
 * {@link #addChildren(String, AbstractSObjectBase, AbstractSObjectBase...)}.
 * <p/>
 * Upon submission to the Salesforce Composite API the {@link SObjectTree} and the {@link SObjectNode}s in it might
 * contain errors that you need to fetch using {@link #getErrors()} method.
 *
 * @see SObjectTree
 * @see RestError
 */
@XStreamAlias("records")
@XStreamConverter(SObjectNodeXStreamConverter.class)
public final class SObjectNode implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty
    final Attributes attributes;

    @JsonProperty
    @JsonUnwrapped
    final AbstractSObjectBase object;

    final Map<String, List<SObjectNode>> records = new HashMap<>();

    private List<RestError> errors;

    @XStreamOmitField
    private final ReferenceGenerator referenceGenerator;

    SObjectNode(final SObjectTree tree, final AbstractSObjectBase object) {
        this(tree.referenceGenerator, typeOf(object), object);
    }

    private SObjectNode(final ReferenceGenerator referenceGenerator, final String type,
            final AbstractSObjectBase object) {
        this.referenceGenerator = requireNonNull(referenceGenerator, "ReferenceGenerator cannot be null");
        this.object = requireNonNull(object, "Root SObject cannot be null");
        attributes = new Attributes(referenceGenerator.nextReferenceFor(object),
            requireNonNull(type, "Object type cannot be null"));
    }

    static String pluralOf(final AbstractDescribedSObjectBase object) {
        final SObjectDescription description = object.description();

        return description.getLabelPlural();
    }

    static String typeOf(final AbstractDescribedSObjectBase object) {
        final SObjectDescription description = object.description();
        return description.getName();
    }

    static String typeOf(final AbstractSObjectBase object) {
        return object.getClass().getSimpleName();
    }

    /**
     * Add a described child with the metadata needed already present within it to the this node.
     *
     * @param child
     *            to add
     * @return the newly created node, used in builder fashion to add more child objects to it (on the next level)
     */
    public SObjectNode addChild(final AbstractDescribedSObjectBase child) {
        ObjectHelper.notNull(child, "child");

        return addChild(pluralOf(child), child);
    }

    /**
     * Add a child that does not contain the required metadata to the this node. You need to specify the plural form of
     * the child (e.g. `Account` its `Accounts`).
     *
     * @param labelPlural
     *            plural form
     * @param child
     *            to add
     * @return the newly created node, used in builder fashion to add more child objects to it (on the next level)
     */
    public SObjectNode addChild(final String labelPlural, final AbstractSObjectBase child) {
        ObjectHelper.notNull(labelPlural, "labelPlural");
        ObjectHelper.notNull(child, "child");

        final SObjectNode node = new SObjectNode(referenceGenerator, typeOf(child), child);

        return addChild(labelPlural, node);
    }

    /**
     * Add multiple described children with the metadata needed already present within them to the this node..
     *
     * @param first
     *            first child to add
     * @param others
     *            any other children to add
     */
    public void addChildren(final AbstractDescribedSObjectBase first, final AbstractDescribedSObjectBase... others) {
        ObjectHelper.notNull(first, "first");
        ObjectHelper.notNull(others, "others");

        addChild(pluralOf(first), first);

        for (final AbstractDescribedSObjectBase other : others) {
            addChild(other);
        }
    }

    /**
     * Add a child that does not contain the required metadata to the this node. You need to specify the plural form of
     * the child (e.g. `Account` its `Accounts`).
     *
     * @param labelPlural
     *            plural form
     * @param first
     *            first child to add
     * @param others
     *            any other children to add
     */
    public void addChildren(final String labelPlural, final AbstractSObjectBase first,
        final AbstractSObjectBase... others) {
        ObjectHelper.notNull(labelPlural, "labelPlural");
        ObjectHelper.notNull(first, "first");
        ObjectHelper.notNull(others, "others");

        addChild(labelPlural, first);

        for (final AbstractSObjectBase other : others) {
            addChild(labelPlural, other);
        }
    }

    @JsonAnyGetter
    public Map<String, Map<String, List<SObjectNode>>> children() {
        final Map<String, Map<String, List<SObjectNode>>> ret = new HashMap<>();
        for (final Map.Entry<String, List<SObjectNode>> entry : records.entrySet()) {
            ret.put(entry.getKey(), Collections.singletonMap("records", entry.getValue()));
        }

        return ret;
    }

    /**
     * Errors reported against this this node received in response to the SObject tree being submitted.
     *
     * @return errors for this node
     */
    @JsonIgnore
    public List<RestError> getErrors() {
        if (errors == null) {
            return Collections.emptyList();
        }

        return errors;
    }

    /**
     * Returns all children of this node (one level deep).
     *
     * @return children of this node
     */
    @JsonIgnore
    public Iterable<SObjectNode> getIterableChildNodes() {
        final Collection<List<SObjectNode>> allNodes = records.values();

        if (allNodes.isEmpty()) {
            return Collections.emptyList();
        }

        final List<SObjectNode> allChildNodes = new ArrayList<>();
        for (final List<SObjectNode> node : allNodes) {
            allChildNodes.addAll(node);
        }

        return allChildNodes;
    }

    /**
     * Returns all children of this node (one level deep) of certain type (in plural form).
     *
     * @param type
     *            type of child requested in plural form (e.g for `Account` is `Accounts`)
     * @return children of this node of specified type
     */
    public Iterable<SObjectNode> getIterableChildNodesOfType(final String type) {
        ObjectHelper.notNull(type, "type");

        final List<SObjectNode> recordsOfType = records.get(type);
        if (recordsOfType == null) {
            return Collections.emptyList();
        }

        return recordsOfType;
    }

    /**
     * Returns child SObjects of this node (one level deep).
     *
     * @return child SObjects of this node
     */
    @JsonIgnore
    public Iterable<AbstractSObjectBase> getIterableChildren() {
        final Collection<List<SObjectNode>> allNodes = records.values();

        if (allNodes.isEmpty()) {
            return Collections.emptyList();
        }

        final List<AbstractSObjectBase> allChilden = new ArrayList<>();
        for (final List<SObjectNode> nodes : allNodes) {
            for (final SObjectNode node : nodes) {
                allChilden.add(node.getObject());
            }
        }

        return allChilden;
    }

    /**
     * Returns child SObjects of this node (one level deep) of certain type (in plural form)
     *
     * @param type
     *            type of child requested in plural form (e.g for `Account` is `Accounts`)
     * @return child SObjects of this node
     */
    public Iterable<AbstractSObjectBase> getIterableChildrenOfType(final String type) {
        ObjectHelper.notNull(type, "type");

        final List<SObjectNode> nodes = records.get(type);
        if (nodes.isEmpty()) {
            return Collections.emptyList();
        }

        final List<AbstractSObjectBase> allChilden = new ArrayList<>();
        for (final SObjectNode node : nodes) {
            allChilden.add(node.getObject());
        }

        return allChilden;
    }

    /**
     * SObject at this node.
     *
     * @return SObject
     */
    @JsonIgnore
    public AbstractSObjectBase getObject() {
        return object;
    }

    /**
     * Are there any errors resulted from the submission on this node?
     *
     * @return true if there are errors
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Size of the branch beginning with this node (number of SObjects in it).
     *
     * @return number of objects within this branch
     */
    public int size() {
        int size = 1;
        for (final List<SObjectNode> nodes : records.values()) {
            for (final SObjectNode node : nodes) {
                size += node.size();
            }
        }

        return size;
    }

    @Override
    public String toString() {
        return "Node<" + getObjectType() + ">";
    }

    SObjectNode addChild(final String labelPlural, final SObjectNode node) {
        List<SObjectNode> children = records.get(labelPlural);
        if (children == null) {
            children = new ArrayList<>();
            records.put(labelPlural, children);
        }

        children.add(node);

        return node;
    }

    Attributes getAttributes() {
        return attributes;
    }

    @JsonIgnore
    String getObjectType() {
        return attributes.type;
    }

    Collection<Class<?>> objectTypes() {
        final Set<Class<?>> ret = new HashSet<>();
        ret.add(object.getClass());

        for (final SObjectNode node : getIterableChildNodes()) {
            ret.addAll(node.objectTypes());
        }

        return ret;
    }

    void setErrors(final List<RestError> errors) {
        this.errors = errors;
    }
}
