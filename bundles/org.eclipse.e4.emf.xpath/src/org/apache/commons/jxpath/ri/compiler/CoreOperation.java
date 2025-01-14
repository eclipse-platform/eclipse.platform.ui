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

import org.apache.commons.jxpath.ri.EvalContext;

/**
 * The common subclass for tree elements representing core operations like "+",
 * "- ", "*" etc.
 */
public abstract class CoreOperation extends Operation {

    /** Or precedence */
    protected static final int OR_PRECEDENCE = 0;
    /** And precedence */
    protected static final int AND_PRECEDENCE = 1;
    /** Compare precedence */
    protected static final int COMPARE_PRECEDENCE = 2;
    /** Relational expression precedence */
    protected static final int RELATIONAL_EXPR_PRECEDENCE = 3;
    /** Add/subtract precedence */
    protected static final int ADD_PRECEDENCE = 4;
    /** Multiply/divide/mod precedence */
    protected static final int MULTIPLY_PRECEDENCE = 5;
    /** Negate precedence */
    protected static final int NEGATE_PRECEDENCE = 6;
    /** Union precedence */
    protected static final int UNION_PRECEDENCE = 7;

    /**
     * Create a new CoreOperation.
     * @param args Expression[]
     */
    public CoreOperation(final Expression[] args) {
        super(args);
    }

    @Override
    public Object compute(final EvalContext context) {
        return computeValue(context);
    }

    @Override
    public abstract Object computeValue(EvalContext context);

    /**
     * Returns the XPath symbol for this operation, e.g. "+", "div", etc.
     * @return String symbol
     */
    public abstract String getSymbol();

    /**
     * Returns true if the operation is not sensitive to the order of arguments,
     * e.g. "=", "and" etc, and false if it is, e.g. "&lt;=", "div".
     * @return boolean
     */
    protected abstract boolean isSymmetric();

    /**
     * Computes the precedence of the operation.
     * @return int precedence
     */
    protected abstract int getPrecedence();

    @Override
    public String toString() {
        if (args.length == 1) {
            return getSymbol() + parenthesize(args[0], false);
        }
        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                buffer.append(' ');
                buffer.append(getSymbol());
                buffer.append(' ');
            }
            buffer.append(parenthesize(args[i], i == 0));
        }
        return buffer.toString();
    }

    /**
     * Wrap an expression in parens if necessary.
     * @param expression other Expression
     * @param left whether {@code expression} is left of this one.
     * @return String
     */
    private String parenthesize(final Expression expression, final boolean left) {
        final String s = expression.toString();
        if (!(expression instanceof CoreOperation)) {
            return s;
        }
        final int compared = getPrecedence() - ((CoreOperation) expression).getPrecedence();

        if (compared < 0) {
            return s;
        }
        if (compared == 0 && (isSymmetric() || left)) {
            return s;
        }
        return '(' + s + ')';
    }
}
