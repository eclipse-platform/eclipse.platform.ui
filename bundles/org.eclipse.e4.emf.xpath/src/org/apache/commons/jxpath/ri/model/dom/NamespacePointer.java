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
package org.apache.commons.jxpath.ri.model.dom;

import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * Represents a namespace node.
 */
public class NamespacePointer extends NodePointer {
    private final String prefix;
    private String namespaceURI;

    private static final long serialVersionUID = -7622456151550131709L;

    /**
     * Create a new NamespacePointer.
     * @param parent parent pointer
     * @param prefix associated ns prefix.
     */
    public NamespacePointer(final NodePointer parent, final String prefix) {
        super(parent);
        this.prefix = prefix;
    }

    /**
     * Create a new NamespacePointer.
     * @param parent parent pointer
     * @param prefix associated ns prefix.
     * @param namespaceURI associated ns URI.
     */
    public NamespacePointer(
        final NodePointer parent,
        final String prefix,
        final String namespaceURI) {
        super(parent);
        this.prefix = prefix;
        this.namespaceURI = namespaceURI;
    }

    @Override
    public QName getName() {
        return new QName(prefix);
    }

    @Override
    public Object getBaseValue() {
        return null;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public Object getImmediateNode() {
        return getNamespaceURI();
    }

    @Override
    public String getNamespaceURI() {
        if (namespaceURI == null) {
            namespaceURI = parent.getNamespaceURI(prefix);
        }
        return namespaceURI;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    /**
     * Throws UnsupportedOperationException.
     * @param value Object
     */
    @Override
    public void setValue(final Object value) {
        throw new UnsupportedOperationException("Cannot modify DOM trees");
    }

    @Override
    public boolean testNode(final NodeTest nodeTest) {
        return nodeTest == null
            || nodeTest instanceof NodeTypeTest
                && ((NodeTypeTest) nodeTest).getNodeType()
                    == Compiler.NODE_TYPE_NODE;
    }

    @Override
    public String asPath() {
        final StringBuilder buffer = new StringBuilder();
        if (parent != null) {
            buffer.append(parent.asPath());
            if (buffer.length() == 0
                || buffer.charAt(buffer.length() - 1) != '/') {
                buffer.append('/');
            }
        }
        buffer.append("namespace::");
        buffer.append(prefix);
        return buffer.toString();
    }

    @Override
    public int hashCode() {
        return prefix.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof NamespacePointer)) {
            return false;
        }

        final NamespacePointer other = (NamespacePointer) object;
        return prefix.equals(other.prefix);
    }

    @Override
    public int compareChildNodePointers(
        final NodePointer pointer1,
        final NodePointer pointer2) {
        // Won't happen - namespaces don't have children
        return 0;
    }
}
