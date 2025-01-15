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

import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.w3c.dom.Node;

/**
 * An iterator of children of a DOM Node.
 */
public class DOMNodeIterator implements NodeIterator {
    private final NodePointer parent;
    private final NodeTest nodeTest;
    private final Node node;
    private Node child = null;
    private final boolean reverse;
    private int position = 0;

    /**
     * Create a new DOMNodeIterator.
     * @param parent parent pointer
     * @param nodeTest test
     * @param reverse whether to iterate in reverse
     * @param startWith starting pointer
     */
    public DOMNodeIterator(
        final NodePointer parent,
        final NodeTest nodeTest,
        final boolean reverse,
        final NodePointer startWith) {
        this.parent = parent;
        this.node = (Node) parent.getNode();
        if (startWith != null) {
            this.child = (Node) startWith.getNode();
        }
        this.nodeTest = nodeTest;
        this.reverse = reverse;
    }

    @Override
    public NodePointer getNodePointer() {
        if (position == 0) {
            setPosition(1);
        }
        return child == null ? null : new DOMNodePointer(parent, child);
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public boolean setPosition(final int position) {
        while (this.position < position) {
            if (!next()) {
                return false;
            }
        }
        while (this.position > position) {
            if (!previous()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets the previous position.
     * @return whether valid
     */
    private boolean previous() {
        position--;
        if (!reverse) {
            if (position == 0) {
                child = null;
            }
            else if (child == null) {
                child = node.getLastChild();
            }
            else {
                child = child.getPreviousSibling();
            }
            while (child != null && !testChild()) {
                child = child.getPreviousSibling();
            }
        }
        else {
            child = child.getNextSibling();
            while (child != null && !testChild()) {
                child = child.getNextSibling();
            }
        }
        return child != null;
    }

    /**
     * Sets the next position.
     * @return whether valid
     */
    private boolean next() {
        position++;
        if (!reverse) {
            if (position == 1 && child == null) {
                child = node.getFirstChild();
            }
            else {
                child = child.getNextSibling();
            }
            while (child != null && !testChild()) {
                child = child.getNextSibling();
            }
        }
        else {
            if (position == 1 && child == null) {
                child = node.getLastChild();
            }
            else {
                child = child.getPreviousSibling();
            }
            while (child != null && !testChild()) {
                child = child.getPreviousSibling();
            }
        }
        return child != null;
    }

    /**
     * Test child.
     * @return result of the test
     */
    private boolean testChild() {
        return DOMNodePointer.testNode(child, nodeTest);
    }
}
