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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * An iterator of attributes of a DOM Node.
 */
public class DOMAttributeIterator implements NodeIterator {
    private final NodePointer parent;
    private final QName name;
    private final List attributes;
    private int position = 0;

    /**
     * Create a new DOMAttributeIterator.
     * @param parent pointer
     * @param name to test
     */
    public DOMAttributeIterator(final NodePointer parent, final QName name) {
        this.parent = parent;
        this.name = name;
        attributes = new ArrayList();
        final Node node = (Node) parent.getNode();
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            final String lname = name.getName();
            if (!lname.equals("*")) {
                final Attr attr = getAttribute((Element) node, name);
                if (attr != null) {
                    attributes.add(attr);
                }
            }
            else {
                final NamedNodeMap map = node.getAttributes();
                final int count = map.getLength();
                for (int i = 0; i < count; i++) {
                    final Attr attr = (Attr) map.item(i);
                    if (testAttr(attr)) {
                        attributes.add(attr);
                    }
                }
            }
        }
    }

    /**
     * Test an attribute.
     * @param attr to test
     * @return whether test succeeded
     */
    private boolean testAttr(final Attr attr) {
        final String nodePrefix = DOMNodePointer.getPrefix(attr);
        final String nodeLocalName = DOMNodePointer.getLocalName(attr);

        if (nodePrefix != null && nodePrefix.equals("xmlns")) {
            return false;
        }

        if (nodePrefix == null && nodeLocalName.equals("xmlns")) {
            return false;
        }

        final String testLocalName = name.getName();
        if (testLocalName.equals("*") || testLocalName.equals(nodeLocalName)) {
            final String testPrefix = name.getPrefix();

            if (testPrefix == null || Objects.equals(testPrefix, nodePrefix)) {
                return true;
            }
            if (nodePrefix == null) {
                return false;
            }
            return Objects.equals(parent.getNamespaceURI(testPrefix), parent
                        .getNamespaceURI(nodePrefix));
        }
        return false;
    }

    /**
     * Gets the named attribute.
     * @param element to search
     * @param name to match
     * @return Attr found
     */
    private Attr getAttribute(final Element element, final QName name) {
        final String testPrefix = name.getPrefix();
        String testNS = null;

        if (testPrefix != null) {
            testNS = parent.getNamespaceResolver().getNamespaceURI(testPrefix);
        }

        if (testNS != null) {
            Attr attr = element.getAttributeNodeNS(testNS, name.getName());
            if (attr != null) {
                return attr;
            }

            // This may mean that the parser does not support NS for
            // attributes, example - the version of Crimson bundled
            // with JDK 1.4.0
            final NamedNodeMap nnm = element.getAttributes();
            for (int i = 0; i < nnm.getLength(); i++) {
                attr = (Attr) nnm.item(i);
                if (testAttr(attr)) {
                    return attr;
                }
            }
            return null;
        }
        return element.getAttributeNode(name.getName());
    }

    @Override
    public NodePointer getNodePointer() {
        if (position == 0) {
            if (!setPosition(1)) {
                return null;
            }
            position = 0;
        }
        int index = position - 1;
        if (index < 0) {
            index = 0;
        }
        return new DOMAttributePointer(parent, (Attr) attributes.get(index));
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public boolean setPosition(final int position) {
        this.position = position;
        return position >= 1 && position <= attributes.size();
    }
}
