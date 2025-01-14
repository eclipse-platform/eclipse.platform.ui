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
package org.apache.commons.jxpath.ri.model.beans;

import java.util.Locale;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.util.ValueUtils;

/**
 * Transparent pointer to a collection (array or Collection).
 */
public class CollectionPointer extends NodePointer {
    private Object collection;
    private NodePointer valuePointer;

    private static final long serialVersionUID = 8620254915563256588L;

    /**
     * Create a new CollectionPointer.
     * @param collection value
     * @param locale Locale
     */
    public CollectionPointer(final Object collection, final Locale locale) {
        super(null, locale);
        this.collection = collection;
    }

    /**
     * Create a new CollectionPointer.
     * @param parent parent NodePointer
     * @param collection value
     */
    public CollectionPointer(final NodePointer parent, final Object collection) {
        super(parent);
        this.collection = collection;
    }

    @Override
    public QName getName() {
        return null;
    }

    @Override
    public Object getBaseValue() {
        return collection;
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public int getLength() {
        return ValueUtils.getLength(getBaseValue());
    }

    @Override
    public boolean isLeaf() {
        final Object value = getNode();
        return value == null || JXPathIntrospector.getBeanInfo(value.getClass()).isAtomic();
    }

    @Override
    public boolean isContainer() {
        return index != WHOLE_COLLECTION;
    }

    @Override
    public Object getImmediateNode() {
        return index == WHOLE_COLLECTION ? ValueUtils.getValue(collection)
                : ValueUtils.getValue(collection, index);
    }

    @Override
    public void setValue(final Object value) {
        if (index == WHOLE_COLLECTION) {
            parent.setValue(value);
        }
        else {
            ValueUtils.setValue(collection, index, value);
        }
    }

    @Override
    public void setIndex(final int index) {
        super.setIndex(index);
        valuePointer = null;
    }

    @Override
    public NodePointer getValuePointer() {
        if (valuePointer == null) {
            if (index == WHOLE_COLLECTION) {
                valuePointer = this;
            }
            else {
                final Object value = getImmediateNode();
                valuePointer = newChildNodePointer(this, getName(), value);
            }
        }
        return valuePointer;
    }

    @Override
    public NodePointer createPath(final JXPathContext context) {
        if (ValueUtils.getLength(getBaseValue()) <= index) {
            collection = ValueUtils.expandCollection(getNode(), index + 1);
        }
        return this;
    }

    @Override
    public NodePointer createPath(final JXPathContext context, final Object value) {
        final NodePointer ptr = createPath(context);
        ptr.setValue(value);
        return ptr;
    }

    @Override
    public NodePointer createChild(
        final JXPathContext context,
        final QName name,
        final int index,
        final Object value) {
        final NodePointer ptr = (NodePointer) clone();
        ptr.setIndex(index);
        return ptr.createPath(context, value);
    }

    @Override
    public NodePointer createChild(
        final JXPathContext context,
        final QName name,
        final int index) {
        final NodePointer ptr = (NodePointer) clone();
        ptr.setIndex(index);
        return ptr.createPath(context);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(collection) + index;
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof CollectionPointer)) {
            return false;
        }

        final CollectionPointer other = (CollectionPointer) object;
        return collection == other.collection && index == other.index;
    }

    @Override
    public NodeIterator childIterator(final NodeTest test,
                final boolean reverse, final NodePointer startWith) {
        if (index == WHOLE_COLLECTION) {
            return new CollectionChildNodeIterator(
                this,
                test,
                reverse,
                startWith);
        }
        return getValuePointer().childIterator(test, reverse, startWith);
    }

    @Override
    public NodeIterator attributeIterator(final QName name) {
        return index == WHOLE_COLLECTION ? new CollectionAttributeNodeIterator(this, name)
                : getValuePointer().attributeIterator(name);
    }

    @Override
    public NodeIterator namespaceIterator() {
        return index == WHOLE_COLLECTION ? null : getValuePointer().namespaceIterator();
    }

    @Override
    public NodePointer namespacePointer(final String namespace) {
        return index == WHOLE_COLLECTION ? null : getValuePointer().namespacePointer(namespace);
    }

    @Override
    public boolean testNode(final NodeTest test) {
        if (index == WHOLE_COLLECTION) {
            if (test == null) {
                return true;
            }
            if (test instanceof NodeNameTest) {
                return false;
            }
            return test instanceof NodeTypeTest && ((NodeTypeTest) test).getNodeType() == Compiler.NODE_TYPE_NODE;
        }
        return getValuePointer().testNode(test);
    }

    @Override
    public int compareChildNodePointers(
                final NodePointer pointer1, final NodePointer pointer2) {
        return pointer1.getIndex() - pointer2.getIndex();
    }

    @Override
    public String asPath() {
        final StringBuilder buffer = new StringBuilder();
        final NodePointer parent = getImmediateParentPointer();
        if (parent != null) {
            buffer.append(parent.asPath());
            if (index != WHOLE_COLLECTION) {
                // Address the list[1][2] case
                if (parent.getIndex() != WHOLE_COLLECTION) {
                    buffer.append("/.");
                }
                buffer.append("[").append(index + 1).append(']');
            }
        }
        else if (index != WHOLE_COLLECTION) {
            buffer.append("/.[").append(index + 1).append(']');
        }
        else {
            buffer.append("/");
        }
        return buffer.toString();
    }
}
