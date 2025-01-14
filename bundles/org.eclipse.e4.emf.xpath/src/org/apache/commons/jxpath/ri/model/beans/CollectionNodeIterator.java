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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * Combines node iterators of all elements of a collection into one
 * aggregate node iterator.
 */
public abstract class CollectionNodeIterator implements NodeIterator {
    private final CollectionPointer pointer;
    private final boolean reverse;
    private final NodePointer startWith;
    private int position;
    private List collection;

    /**
     * Create a new CollectionNodeIterator.
     * @param pointer collection pointer
     * @param reverse iteration order
     * @param startWith starting pointer
     */
    protected CollectionNodeIterator(
        final CollectionPointer pointer,
        final boolean reverse,
        final NodePointer startWith) {
        this.pointer = pointer;
        this.reverse = reverse;
        this.startWith = startWith;
    }

    /**
     * Implemented by subclasses to produce child/attribute node iterators.
     * @param elementPointer owning pointer
     * @return NodeIterator
     */
    protected abstract NodeIterator
            getElementNodeIterator(NodePointer elementPointer);

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public boolean setPosition(final int position) {
        if (collection == null) {
            prepare();
        }

        if (position < 1 || position > collection.size()) {
            return false;
        }
        this.position = position;
        return true;
    }

    @Override
    public NodePointer getNodePointer() {
        if (position == 0) {
            return null;
        }
        return (NodePointer) collection.get(position - 1);
    }

    /**
     * Prepare...
     */
    private void prepare() {
        collection = new ArrayList();
        final NodePointer ptr = (NodePointer) pointer.clone();
        final int length = ptr.getLength();
        for (int i = 0; i < length; i++) {
            ptr.setIndex(i);
            final NodePointer elementPointer = ptr.getValuePointer();
            final NodeIterator iter = getElementNodeIterator(elementPointer);

            for (int j = 1; iter.setPosition(j); j++) {
                final NodePointer childPointer = iter.getNodePointer();
                if (reverse) {
                    collection.add(0, childPointer);
                }
                else {
                    collection.add(childPointer);
                }
            }
        }
        if (startWith != null) {
            final int index = collection.indexOf(startWith);
            if (index == -1) {
                throw new JXPathException(
                    "Invalid starting pointer for iterator: " + startWith);
            }
            while (collection.size() > index) {
                if (!reverse) {
                    collection.remove(collection.size() - 1);
                }
                else {
                    collection.remove(0);
                }
            }
        }
    }
}
