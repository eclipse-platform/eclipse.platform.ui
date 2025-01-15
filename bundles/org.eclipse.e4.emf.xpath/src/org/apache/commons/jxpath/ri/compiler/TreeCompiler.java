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

import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;

/**
 */
public class TreeCompiler implements Compiler {

    private static final QName QNAME_NAME = new QName(null, "name");

    @Override
    public Object number(final String value) {
        return new Constant(Double.valueOf(value));
    }

    @Override
    public Object literal(final String value) {
        return new Constant(value);
    }

    @Override
    public Object qname(final String prefix, final String name) {
        return new QName(prefix, name);
    }

    @Override
    public Object sum(final Object[] arguments) {
        return new CoreOperationAdd(toExpressionArray(arguments));
    }

    @Override
    public Object minus(final Object left, final Object right) {
        return new CoreOperationSubtract(
            (Expression) left,
            (Expression) right);
    }

    @Override
    public Object multiply(final Object left, final Object right) {
        return new CoreOperationMultiply((Expression) left, (Expression) right);
    }

    @Override
    public Object divide(final Object left, final Object right) {
        return new CoreOperationDivide((Expression) left, (Expression) right);
    }

    @Override
    public Object mod(final Object left, final Object right) {
        return new CoreOperationMod((Expression) left, (Expression) right);
    }

    @Override
    public Object lessThan(final Object left, final Object right) {
        return new CoreOperationLessThan((Expression) left, (Expression) right);
    }

    @Override
    public Object lessThanOrEqual(final Object left, final Object right) {
        return new CoreOperationLessThanOrEqual(
            (Expression) left,
            (Expression) right);
    }

    @Override
    public Object greaterThan(final Object left, final Object right) {
        return new CoreOperationGreaterThan(
            (Expression) left,
            (Expression) right);
    }

    @Override
    public Object greaterThanOrEqual(final Object left, final Object right) {
        return new CoreOperationGreaterThanOrEqual(
            (Expression) left,
            (Expression) right);
    }

    @Override
    public Object equal(final Object left, final Object right) {
        return isNameAttributeTest((Expression) left)
                ? new NameAttributeTest((Expression) left, (Expression) right)
                : new CoreOperationEqual((Expression) left, (Expression) right);
    }

    @Override
    public Object notEqual(final Object left, final Object right) {
        return new CoreOperationNotEqual((Expression) left, (Expression) right);
    }

    @Override
    public Object minus(final Object argument) {
        return new CoreOperationNegate((Expression) argument);
    }

    @Override
    public Object variableReference(final Object qName) {
        return new VariableReference((QName) qName);
    }

    @Override
    public Object function(final int code, final Object[] args) {
        return new CoreFunction(code, toExpressionArray(args));
    }

    @Override
    public Object function(final Object name, final Object[] args) {
        return new ExtensionFunction((QName) name, toExpressionArray(args));
    }

    @Override
    public Object and(final Object[] arguments) {
        return new CoreOperationAnd(toExpressionArray(arguments));
    }

    @Override
    public Object or(final Object[] arguments) {
        return new CoreOperationOr(toExpressionArray(arguments));
    }

    @Override
    public Object union(final Object[] arguments) {
        return new CoreOperationUnion(toExpressionArray(arguments));
    }

    @Override
    public Object locationPath(final boolean absolute, final Object[] steps) {
        return new LocationPath(absolute, toStepArray(steps));
    }

    @Override
    public Object expressionPath(final Object expression, final Object[] predicates,
            final Object[] steps) {
        return new ExpressionPath(
            (Expression) expression,
            toExpressionArray(predicates),
            toStepArray(steps));
    }

    @Override
    public Object nodeNameTest(final Object qname) {
        return new NodeNameTest((QName) qname);
    }

    @Override
    public Object nodeTypeTest(final int nodeType) {
        return new NodeTypeTest(nodeType);
    }

    @Override
    public Object processingInstructionTest(final String instruction) {
        return new ProcessingInstructionTest(instruction);
    }

    @Override
    public Object step(final int axis, final Object nodeTest, final Object[] predicates) {
        return new Step(
            axis,
            (NodeTest) nodeTest,
            toExpressionArray(predicates));
    }

    /**
     * Gets an Object[] as an Expression[].
     * @param array Object[]
     * @return Expression[]
     */
    private Expression[] toExpressionArray(final Object[] array) {
        Expression[] expArray = null;
        if (array != null) {
            expArray = new Expression[array.length];
            for (int i = 0; i < expArray.length; i++) {
                expArray[i] = (Expression) array[i];
            }
        }
        return expArray;
    }

    /**
     * Gets an Object[] as a Step[].
     * @param array Object[]
     * @return Step[]
     */
    private Step[] toStepArray(final Object[] array) {
        Step[] stepArray = null;
        if (array != null) {
            stepArray = new Step[array.length];
            for (int i = 0; i < stepArray.length; i++) {
                stepArray[i] = (Step) array[i];
            }
        }
        return stepArray;
    }

    /**
     * Learn whether arg is a name attribute test.
     * @param arg Expression to test
     * @return boolean
     */
    private boolean isNameAttributeTest(final Expression arg) {
        if (!(arg instanceof LocationPath)) {
            return false;
        }

        final Step[] steps = ((LocationPath) arg).getSteps();
        if (steps.length != 1) {
            return false;
        }
        if (steps[0].getAxis() != AXIS_ATTRIBUTE) {
            return false;
        }
        final NodeTest test = steps[0].getNodeTest();
        if (!(test instanceof NodeNameTest)) {
            return false;
        }
        if (!((NodeNameTest) test).getNodeName().equals(QNAME_NAME)) {
            return false;
        }
        return true;
    }
}
