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
package org.apache.commons.jxpath.ri.compiler;

import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.util.ValueUtils;

/**
 * Common superclass for several types of nodes in the parse tree. Provides
 * APIs for optimization of evaluation of expressions.  Specifically, an
 * expression only needs to executed once during the evaluation of an xpath
 * if that expression is context-independent.  Expression.isContextDependent()
 * provides that hint.
 */
public abstract class Expression {

    /** Zero */
    protected static final Double ZERO = Double.valueOf(0);

    /** One */
    protected static final Double ONE = Double.valueOf(1);

    /** NaN */
    protected static final Double NOT_A_NUMBER = Double.valueOf(Double.NaN);

    private boolean contextDependencyKnown = false;
    private boolean contextDependent;

    /**
     * Returns true if this expression should be re-evaluated
     * each time the current position in the context changes.
     * @return boolean
     */
    public synchronized boolean isContextDependent() {
        if (!contextDependencyKnown) {
            contextDependent = computeContextDependent();
            contextDependencyKnown = true;
        }
        return contextDependent;
    }

    /**
     * Implemented by subclasses and result is cached by isContextDependent()
     * @return calculated context-dependentness as boolean
     */
    public abstract boolean computeContextDependent();

    /**
     * Evaluates the expression. If the result is a node set, returns
     * the first element of the node set.
     * @param context evaluation context
     * @return Object
     */
    public abstract Object computeValue(EvalContext context);

    /**
     * Evaluates the expression. If the result is a node set, returns
     * the first element of the node set.
     * @param context evaluation context
     * @return Object
     */
    public abstract Object compute(EvalContext context);

    /**
     * Iterate over the values from the specified context.
     * @param context evaluation context
     * @return value Iterator
     */
    public Iterator iterate(final EvalContext context) {
        final Object result = compute(context);
        if (result instanceof EvalContext) {
            return new ValueIterator((EvalContext) result);
        }
        if (result instanceof NodeSet) {
            return new ValueIterator(((NodeSet) result).getPointers().iterator());
        }
        return ValueUtils.iterate(result);
    }

    /**
     * Iterate over the pointers from the specified context.
     * @param context evaluation context
     * @return pointer Iterator
     */
    public Iterator iteratePointers(final EvalContext context) {
        final Object result = compute(context);
        if (result == null) {
            return Collections.EMPTY_LIST.iterator();
        }
        if (result instanceof EvalContext) {
            return (EvalContext) result;
        }
        if (result instanceof NodeSet) {
            return new PointerIterator(((NodeSet) result).getPointers().iterator(),
                    new QName(null, "value"),
                    context.getRootContext().getCurrentNodePointer().getLocale());
        }
        return new PointerIterator(ValueUtils.iterate(result),
                new QName(null, "value"),
                context.getRootContext().getCurrentNodePointer().getLocale());
    }

    /**
     * Pointer iterator
     */
    public static class PointerIterator implements Iterator {
        private final Iterator iterator;
        private final QName qname;
        private final Locale locale;

        //to what method does the following comment refer?
        /**
         * Create a new PointerIterator
         * @param it underlying Iterator
         * @param qname name
         * @param locale Locale
         * @deprecated Use the method that takes a NamespaceManager
         */
        @Deprecated
        public PointerIterator(final Iterator it, final QName qname, final Locale locale) {
            this.iterator = it;
            this.qname = qname;
            this.locale = locale;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Object next() {
            final Object o = iterator.next();
            return o instanceof Pointer ? o : NodePointer.newNodePointer(qname, o, locale);
        }

        /**
         * Unsupported.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Value Iterator
     */
    public static class ValueIterator implements Iterator {
        private final Iterator iterator;

        /**
         * Create a new ValueIterator.
         * @param it underlying Iterator, may contain pointers
         */
        public ValueIterator(final Iterator it) {
            this.iterator = it;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Object next() {
            final Object o = iterator.next();
            return o instanceof Pointer ? ((Pointer) o).getValue() : o;
        }

        /**
         * Unsupported.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
