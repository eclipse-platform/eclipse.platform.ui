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
package org.apache.commons.jxpath.ri.axes;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * EvalContext that can walk the "child::", "following-sibling::" and
 * "preceding-sibling::" axes.
 */
public class ChildContext extends EvalContext {
    private final NodeTest nodeTest;
    private final boolean startFromParentLocation;
    private final boolean reverse;
    private NodeIterator iterator;

    /**
     * Create a new ChildContext.
     * @param parentContext parent EvalContext
     * @param nodeTest NodeTest
     * @param startFromParentLocation whether to start from parent location
     * @param reverse whether to iterate in reverse
     */
    public ChildContext(final EvalContext parentContext, final NodeTest nodeTest,
            final boolean startFromParentLocation, final boolean reverse) {
        super(parentContext);
        this.nodeTest = nodeTest;
        this.startFromParentLocation = startFromParentLocation;
        this.reverse = reverse;
    }

    @Override
    public NodePointer getCurrentNodePointer() {
        if (position == 0 && !setPosition(1)) {
            return null;
        }
        return iterator == null ? null : iterator.getNodePointer();
    }

    /**
     * This method is called on the last context on the path when only
     * one value is needed.  Note that this will return the whole property,
     * even if it is a collection. It will not extract the first element
     * of the collection.  For example, "books" will return the collection
     * of books rather than the first book from that collection.
     * @return Pointer
     */
    @Override
    public Pointer getSingleNodePointer() {
        if (position == 0) {
            while (nextSet()) {
                prepare();
                if (iterator == null) {
                    return null;
                }
                // See if there is a property there, singular or collection
                final NodePointer pointer = iterator.getNodePointer();
                if (pointer != null) {
                    return pointer;
                }
            }
            return null;
        }
        return getCurrentNodePointer();
    }

    @Override
    public boolean nextNode() {
        return setPosition(getCurrentPosition() + 1);
    }

    @Override
    public void reset() {
        super.reset();
        iterator = null;
    }

    @Override
    public boolean setPosition(final int position) {
        final int oldPosition = getCurrentPosition();
        super.setPosition(position);
        if (oldPosition == 0) {
            prepare();
        }
        return iterator != null && iterator.setPosition(position);
    }

    /**
     * Allocates a PropertyIterator.
     */
    private void prepare() {
        final NodePointer parent = parentContext.getCurrentNodePointer();
        if (parent == null) {
            return;
        }
        final NodePointer useParent = startFromParentLocation ? parent.getParent() : parent;
        iterator = useParent == null ? null : useParent.childIterator(nodeTest, reverse,
                startFromParentLocation ? parent : null);
    }
}
