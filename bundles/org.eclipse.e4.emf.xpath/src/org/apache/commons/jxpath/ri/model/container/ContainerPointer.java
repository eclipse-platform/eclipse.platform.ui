/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.ri.model.container;

import java.util.Locale;

import org.apache.commons.jxpath.Container;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.util.ValueUtils;

/**
 * Transparent pointer to a Container. The {@link #getValue()} method
 * returns the contents of the container, rather than the container
 * itself.
 */
public class ContainerPointer extends NodePointer {
    private final Container container;
    private NodePointer valuePointer;

    private static final long serialVersionUID = 6140752946621686118L;

    /**
     * Create a new ContainerPointer.
     * @param container Container object
     * @param locale Locale
     */
    public ContainerPointer(final Container container, final Locale locale) {
        super(null, locale);
        this.container = container;
    }

    /**
     * Create a new ContainerPointer.
     * @param parent parent pointer
     * @param container Container object
     */
    public ContainerPointer(final NodePointer parent, final Container container) {
        super(parent);
        this.container = container;
    }

    /**
     * This type of node is auxiliary.
     * @return {@code true}.
     */
    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public QName getName() {
        return null;
    }

    @Override
    public Object getBaseValue() {
        return container;
    }

    @Override
    public boolean isCollection() {
        final Object value = getBaseValue();
        return value != null && ValueUtils.isCollection(value);
    }

    @Override
    public int getLength() {
        final Object value = getBaseValue();
        return value == null ? 1 : ValueUtils.getLength(value);
    }

    @Override
    public boolean isLeaf() {
        return getValuePointer().isLeaf();
    }

    @Override
    public Object getImmediateNode() {
        final Object value = getBaseValue();
        if (index != WHOLE_COLLECTION) {
            return index >= 0 && index < getLength() ? ValueUtils.getValue(value, index) : null;
        }
        return ValueUtils.getValue(value);
    }

    @Override
    public void setValue(final Object value) {
        // TODO: what if this is a collection?
        container.setValue(value);
    }

    @Override
    public NodePointer getImmediateValuePointer() {
        if (valuePointer == null) {
            final Object value = getImmediateNode();
            valuePointer = newChildNodePointer(this, getName(), value);
        }
        return valuePointer;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(container) + index;
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof ContainerPointer)) {
            return false;
        }

        final ContainerPointer other = (ContainerPointer) object;
        return container == other.container && index == other.index;
    }

    @Override
    public NodeIterator childIterator(
        final NodeTest test,
        final boolean reverse,
        final NodePointer startWith) {
        return getValuePointer().childIterator(test, reverse, startWith);
    }

    @Override
    public NodeIterator attributeIterator(final QName name) {
        return getValuePointer().attributeIterator(name);
    }

    @Override
    public NodeIterator namespaceIterator() {
        return getValuePointer().namespaceIterator();
    }

    @Override
    public NodePointer namespacePointer(final String namespace) {
        return getValuePointer().namespacePointer(namespace);
    }

    @Override
    public boolean testNode(final NodeTest nodeTest) {
        return getValuePointer().testNode(nodeTest);
    }

    @Override
    public int compareChildNodePointers(
        final NodePointer pointer1,
        final NodePointer pointer2) {
        return pointer1.getIndex() - pointer2.getIndex();
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        return getValuePointer().getNamespaceURI(prefix);
    }

    @Override
    public String asPath() {
        return parent == null ? "/" : parent.asPath();
    }
}
