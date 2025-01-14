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

import java.util.Iterator;

import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.compiler.Expression;

/**
 * RI of CompiledExpression.
 */
public class JXPathCompiledExpression implements CompiledExpression {

    private final String xpath;
    private final Expression expression;

    /**
     * Create a new JXPathCompiledExpression.
     * @param xpath source
     * @param expression compiled
     */
    public JXPathCompiledExpression(final String xpath, final Expression expression) {
        this.xpath = xpath;
        this.expression = expression;
    }

    /**
     * Gets the source expression.
     * @return String
     */
    protected String getXPath() {
        return xpath;
    }

    /**
     * Gets the compiled expression.
     * @return Expression
     */
    protected Expression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return xpath;
    }

    @Override
    public Object getValue(final JXPathContext context) {
        return ((JXPathContextReferenceImpl) context).
                    getValue(xpath, expression);
    }

    @Override
    public Object getValue(final JXPathContext context, final Class requiredType) {
        return ((JXPathContextReferenceImpl) context).
                    getValue(xpath, expression, requiredType);
    }

    @Override
    public void setValue(final JXPathContext context, final Object value) {
        ((JXPathContextReferenceImpl) context).
                    setValue(xpath, expression, value);
    }

    @Override
    public Pointer createPath(final JXPathContext context) {
        return ((JXPathContextReferenceImpl) context).
                    createPath(xpath, expression);
    }

    @Override
    public Pointer createPathAndSetValue(final JXPathContext context, final Object value) {
        return ((JXPathContextReferenceImpl) context).
                    createPathAndSetValue(xpath, expression, value);
    }

    @Override
    public Iterator iterate(final JXPathContext context) {
        return ((JXPathContextReferenceImpl) context).
                    iterate(xpath, expression);
    }

    @Override
    public Pointer getPointer(final JXPathContext context, final String xpath) {
        return ((JXPathContextReferenceImpl) context).
                    getPointer(xpath, expression);
    }

    @Override
    public Iterator iteratePointers(final JXPathContext context) {
        return ((JXPathContextReferenceImpl) context).
                    iteratePointers(xpath, expression);
    }

    @Override
    public void removePath(final JXPathContext context) {
        ((JXPathContextReferenceImpl) context).removePath(xpath, expression);
    }

    @Override
    public void removeAll(final JXPathContext context) {
        ((JXPathContextReferenceImpl) context).removeAll(xpath, expression);
    }
}
