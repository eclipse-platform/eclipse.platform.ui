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

import java.util.Iterator;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.NameAttributeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyOwnerPointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyPointer;

/**
 * EvalContext that checks predicates.
 */
public class PredicateContext extends EvalContext {
    private final Expression expression;
    private boolean done = false;
    private Expression nameTestExpression;
    private PropertyPointer dynamicPropertyPointer;

    /**
     * Create a new PredicateContext.
     * @param parentContext parent context
     * @param expression compiled Expression
     */
    public PredicateContext(final EvalContext parentContext, final Expression expression) {
        super(parentContext);
        this.expression = expression;
        if (expression instanceof NameAttributeTest) {
            nameTestExpression =
                ((NameAttributeTest) expression).getNameTestExpression();
        }
    }

    @Override
    public boolean nextNode() {
        if (done) {
            return false;
        }
        while (parentContext.nextNode()) {
            if (setupDynamicPropertyPointer()) {
                final Object pred = nameTestExpression.computeValue(parentContext);
                final String propertyName = InfoSetUtil.stringValue(pred);

                // At this point it would be nice to say:
                // dynamicPropertyPointer.setPropertyName(propertyName)
                // and then: dynamicPropertyPointer.isActual().
                // However some PropertyPointers, e.g. DynamicPropertyPointer
                // will declare that any property you ask for is actual.
                // That's not acceptable for us: we really need to know
                // if the property is currently declared. Thus,
                // we'll need to perform a search.
                boolean ok = false;
                final String[] names = dynamicPropertyPointer.getPropertyNames();
                for (final String name : names) {
                    if (name.equals(propertyName)) {
                        ok = true;
                        break;
                    }
                }
                if (ok) {
                    dynamicPropertyPointer.setPropertyName(propertyName);
                    position++;
                    return true;
                }
            }
            else {
                Object pred = expression.computeValue(parentContext);
                if (pred instanceof Iterator) {
                    if (!((Iterator) pred).hasNext()) {
                        return false;
                    }
                    pred = ((Iterator) pred).next();
                }

                if (pred instanceof NodePointer) {
                    pred = ((NodePointer) pred).getNode();
                }

                if (pred instanceof Number) {
                    final int pos = (int) InfoSetUtil.doubleValue(pred);
                    position++;
                    done = true;
                    return parentContext.setPosition(pos);
                }
                if (InfoSetUtil.booleanValue(pred)) {
                    position++;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Used for an optimized access to dynamic properties using the
     * "map[@name = 'name']" syntax
     * @return whether valid
     */
    private boolean setupDynamicPropertyPointer() {
        if (nameTestExpression == null) {
            return false;
        }

        NodePointer parent = parentContext.getCurrentNodePointer();
        if (parent == null) {
            return false;
        }
        parent = parent.getValuePointer();
        if (!(parent instanceof PropertyOwnerPointer)) {
            return false;
        }
        dynamicPropertyPointer =
            (PropertyPointer) ((PropertyOwnerPointer) parent)
                .getPropertyPointer()
                .clone();
        return true;
    }

    @Override
    public boolean setPosition(final int position) {
        if (nameTestExpression == null) {
            return setPositionStandard(position);
        }
        if (dynamicPropertyPointer == null && !setupDynamicPropertyPointer()) {
            return setPositionStandard(position);
        }
        if (position < 1
            || position > dynamicPropertyPointer.getLength()) {
            return false;
        }
        dynamicPropertyPointer.setIndex(position - 1);
        return true;
    }

    @Override
    public NodePointer getCurrentNodePointer() {
        if (position == 0 && !setPosition(1)) {
            return null;
        }
        if (dynamicPropertyPointer != null) {
            return dynamicPropertyPointer.getValuePointer();
        }
        return parentContext.getCurrentNodePointer();
    }

    @Override
    public void reset() {
        super.reset();
        parentContext.reset();
        done = false;
    }

    @Override
    public boolean nextSet() {
        reset();
        return parentContext.nextSet();
    }

    /**
     * Basic setPosition
     * @param position to set
     * @return whether valid
     */
    private boolean setPositionStandard(final int position) {
        if (this.position > position) {
            reset();
        }

        while (this.position < position) {
            if (!nextNode()) {
                return false;
            }
        }
        return true;
    }
}
