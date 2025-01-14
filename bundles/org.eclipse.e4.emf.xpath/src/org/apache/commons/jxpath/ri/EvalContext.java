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
package org.apache.commons.jxpath.ri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.jxpath.BasicNodeSet;
import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.util.ReverseComparator;

/**
 * An XPath evaluation context.
 *
 * When  evaluating a path, a chain of EvalContexts is created, each context in
 * the chain representing a step of the path. Subclasses of EvalContext
 * implement behavior of various XPath axes: "child::", "parent::" etc.
 */
public abstract class EvalContext implements ExpressionContext, Iterator {
    /** Parent context */
    protected EvalContext parentContext;

    /** Root context */
    protected RootContext rootContext;

    /** Position */
    protected int position = 0;

    private boolean startedSetIteration = false;
    private boolean done = false;
    private boolean hasPerformedIteratorStep = false;
    private Iterator<Pointer> pointerIterator;

    /**
     * Create a new EvalContext.
     * @param parentContext parent context
     */
    public EvalContext(final EvalContext parentContext) {
        this.parentContext = parentContext;
    }

    @Override
    public Pointer getContextNodePointer() {
        return getCurrentNodePointer();
    }

    @Override
    public JXPathContext getJXPathContext() {
        return getRootContext().getJXPathContext();
    }

    @Override
    public int getPosition() {
        return position;
    }

    /**
     * Determines the document order for this context.
     *
     * @return 1 ascending order, -1 descending order,
     *  0 - does not require ordering
     */
    public int getDocumentOrder() {
        return parentContext != null && parentContext.isChildOrderingRequired() ? 1 : 0;
    }

    /**
     * Even if this context has the natural ordering and therefore does
     * not require collecting and sorting all nodes prior to returning them,
     * such operation may be required for any child context.
     * @return boolean
     */
    public boolean isChildOrderingRequired() {
        // Default behavior: if this context needs to be ordered,
        // the children need to be ordered too
        return getDocumentOrder() != 0;
    }

    /**
     * Returns true if there are mode nodes matching the context's constraints.
     * @return boolean
     */
    @Override
    public boolean hasNext() {
        if (pointerIterator != null) {
            return pointerIterator.hasNext();
        }
        if (getDocumentOrder() != 0) {
            return constructIterator();
        }
        if (!done && !hasPerformedIteratorStep) {
            performIteratorStep();
        }
        return !done;
    }

    /**
     * Returns the next node pointer in the context
     * @return Object
     */
    @Override
    public Object next() {
        if (pointerIterator != null) {
            return pointerIterator.next();
        }

        if (getDocumentOrder() != 0) {
            if (!constructIterator()) {
                throw new NoSuchElementException();
            }
            return pointerIterator.next();
        }
        if (!done && !hasPerformedIteratorStep) {
            performIteratorStep();
        }
        if (done) {
            throw new NoSuchElementException();
        }
        hasPerformedIteratorStep = false;
        return getCurrentNodePointer();
    }

    /**
     * Moves the iterator forward by one position
     */
    private void performIteratorStep() {
        done = true;
        if (position != 0 && nextNode()) {
            done = false;
        }
        else {
            while (nextSet()) {
                if (nextNode()) {
                    done = false;
                    break;
                }
            }
        }
        hasPerformedIteratorStep = true;
    }

    /**
     * Operation is not supported
     * @throws UnsupportedOperationException Always thrown.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException(
            "JXPath iterators cannot remove nodes");
    }

    /**
     * Constructs an iterator.
     * @return whether the Iterator was constructed
     */
    private boolean constructIterator() {
        final HashSet<Pointer> set = new HashSet<>();
        final ArrayList<Pointer> list = new ArrayList<>();
        while (nextSet()) {
            while (nextNode()) {
                final NodePointer pointer = getCurrentNodePointer();
                if (!set.contains(pointer)) {
                    set.add(pointer);
                    list.add(pointer);
                }
            }
        }
        if (list.isEmpty()) {
            return false;
        }

        sortPointers(list);

        pointerIterator = list.iterator();
        return true;
    }

    /**
     * Sort a list of pointers based on document order.
     * @param l the list to sort.
     */
    protected void sortPointers(final List l) {
        switch (getDocumentOrder()) {
        case 1:
            Collections.sort(l);
            break;
        case -1:
            Collections.sort(l, ReverseComparator.INSTANCE);
            break;
        default:
            break;
        }
    }

    /**
     * Returns the list of all Pointers in this context for the current
     * position of the parent context.
     * @return List
     */
    @Override
    public List getContextNodeList() {
        final int pos = position;
        if (pos != 0) {
            reset();
        }
        final List<Pointer> list = new ArrayList<>();
        while (nextNode()) {
            list.add(getCurrentNodePointer());
        }
        if (pos != 0) {
            setPosition(pos);
        }
        else {
            reset();
        }
        return list;
    }

    /**
     * Returns the list of all Pointers in this context for all positions
     * of the parent contexts.  If there was an ongoing iteration over
     * this context, the method should not be called.
     * @return NodeSet
     */
    public NodeSet getNodeSet() {
        if (position != 0) {
            throw new JXPathException(
                "Simultaneous operations: "
                    + "should not request pointer list while "
                    + "iterating over an EvalContext");
        }
        final BasicNodeSet set = new BasicNodeSet();
        while (nextSet()) {
            while (nextNode()) {
                set.add((Pointer) getCurrentNodePointer().clone());
            }
        }

        return set;
    }

    /**
     * Typically returns the NodeSet by calling getNodeSet(),
     * but will be overridden for contexts that more naturally produce
     * individual values, e.g. VariableContext
     * @return Object
     */
    public Object getValue() {
        return getNodeSet();
    }

    @Override
    public String toString() {
        final Pointer ptr = getContextNodePointer();
        return ptr == null ? "Empty expression context" : "Expression context [" + getPosition()
                + "] " + ptr.asPath();
    }

    /**
     * Returns the root context of the path, which provides easy
     * access to variables and functions.
     * @return RootContext
     */
    public RootContext getRootContext() {
        if (rootContext == null) {
            rootContext = parentContext.getRootContext();
        }
        return rootContext;
    }

    /**
     * Sets current position = 0, which is the pre-iteration state.
     */
    public void reset() {
        position = 0;
    }

    /**
     * Gets the current position.
     * @return int position.
     */
    public int getCurrentPosition() {
        return position;
    }

    /**
     * Returns the first encountered Pointer that matches the current
     * context's criteria.
     * @return Pointer
     */
    public Pointer getSingleNodePointer() {
        reset();
        while (nextSet()) {
            if (nextNode()) {
                return getCurrentNodePointer();
            }
        }
        return null;
    }

    /**
     * Returns the current context node. Undefined before the beginning
     * of the iteration.
     * @return NodePoiner
     */
    public abstract NodePointer getCurrentNodePointer();

    /**
     * Returns true if there is another sets of objects to interate over.
     * Resets the current position and node.
     * @return boolean
     */
    public boolean nextSet() {
        reset(); // Restart iteration within the set

        // Most of the time you have one set per parent node
        // First time this method is called, we should look for
        // the first parent set that contains at least one node.
        if (!startedSetIteration) {
            startedSetIteration = true;
            while (parentContext.nextSet()) {
                if (parentContext.nextNode()) {
                    return true;
                }
            }
            return false;
        }

        // In subsequent calls, we see if the parent context
        // has any nodes left in the current set
        if (parentContext.nextNode()) {
            return true;
        }

        // If not, we look for the next set that contains
        // at least one node
        while (parentContext.nextSet()) {
            if (parentContext.nextNode()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if there is another object in the current set.
     * Switches the current position and node to the next object.
     * @return boolean
     */
    public abstract boolean nextNode();

    /**
     * Moves the current position to the specified index. Used with integer
     * predicates to quickly get to the n'th element of the node set.
     * Returns false if the position is out of the node set range.
     * You can call it with 0 as the position argument to restart the iteration.
     * @param position to set
     * @return boolean
     */
    public boolean setPosition(final int position) {
        this.position = position;
        return true;
    }
}
