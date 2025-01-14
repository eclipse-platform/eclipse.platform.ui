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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;

/**
 * Base implementation of Expression for the operations "&gt;", "&gt;=", "&lt;", "&lt;=".
 * @since JXPath 1.3
 */
public abstract class CoreOperationRelationalExpression extends CoreOperation {

    /**
     * Create a new CoreOperationRelationalExpression.
     * @param args arguments
     */
    protected CoreOperationRelationalExpression(final Expression[] args) {
        super(args);
    }

    @Override
    public final Object computeValue(final EvalContext context) {
        return compute(args[0].compute(context), args[1].compute(context))
                ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    protected final int getPrecedence() {
        return RELATIONAL_EXPR_PRECEDENCE;
    }

    @Override
    protected final boolean isSymmetric() {
        return false;
    }

    /**
     * Template method for subclasses to evaluate the result of a comparison.
     * @param compare result of comparison to evaluate
     * @return ultimate operation success/failure
     */
    protected abstract boolean evaluateCompare(int compare);

    /**
     * Compare left to right.
     * @param left left operand
     * @param right right operand
     * @return operation success/failure
     */
    private boolean compute(Object left, Object right) {
        left = reduce(left);
        right = reduce(right);

        if (left instanceof InitialContext) {
            ((InitialContext) left).reset();
        }
        if (right instanceof InitialContext) {
            ((InitialContext) right).reset();
        }
        if (left instanceof Iterator && right instanceof Iterator) {
            return findMatch((Iterator) left, (Iterator) right);
        }
        if (left instanceof Iterator) {
            return containsMatch((Iterator) left, right);
        }
        if (right instanceof Iterator) {
            return containsMatch(left, (Iterator) right);
        }
        final double ld = InfoSetUtil.doubleValue(left);
        if (Double.isNaN(ld)) {
            return false;
        }
        final double rd = InfoSetUtil.doubleValue(right);
        if (Double.isNaN(rd)) {
            return false;
        }
        return evaluateCompare(ld == rd ? 0 : ld < rd ? -1 : 1);
    }

    /**
     * Reduce an operand for comparison.
     * @param o Object to reduce
     * @return reduced operand
     */
    private Object reduce(Object o) {
        if (o instanceof SelfContext) {
            o = ((EvalContext) o).getSingleNodePointer();
        }
        if (o instanceof Collection) {
            o = ((Collection) o).iterator();
        }
        return o;
    }

    /**
     * Learn whether any element returned from an Iterator matches a given value.
     * @param it Iterator
     * @param value to look for
     * @return whether a match was found
     */
    private boolean containsMatch(final Iterator it, final Object value) {
        while (it.hasNext()) {
            final Object element = it.next();
            if (compute(element, value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Learn whether any element returned from an Iterator matches a given value.
     * @param it Iterator
     * @param value to look for
     * @return whether a match was found
     */
    private boolean containsMatch(final Object value, final Iterator it) {
        while (it.hasNext()) {
            final Object element = it.next();
            if (compute(value, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Learn whether there is an intersection between two Iterators.
     * @param lit left Iterator
     * @param rit right Iterator
     * @return whether a match was found
     */
    private boolean findMatch(final Iterator lit, final Iterator rit) {
        final HashSet left = new HashSet();
        while (lit.hasNext()) {
            left.add(lit.next());
        }
        while (rit.hasNext()) {
            if (containsMatch(left.iterator(), rit.next())) {
                return true;
            }
        }
        return false;
    }

}
