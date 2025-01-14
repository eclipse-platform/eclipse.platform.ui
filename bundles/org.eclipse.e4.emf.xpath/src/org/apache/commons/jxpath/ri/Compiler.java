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

/**
 * The Compiler APIs are completely agnostic to the actual types of objects
 * produced and consumed by the APIs.  Arguments and return values are
 * declared as java.lang.Object.
 * <p>
 * Since objects returned by Compiler methods are passed as arguments to other
 * Compiler methods, the descriptions of these methods use virtual types.  There
 * are four virtual object types: EXPRESSION, QNAME, STEP and NODE_TEST.
 * <p>
 * The following example illustrates this notion.  This sequence compiles
 * the xpath "foo[round(1 div 2)]/text()":
 * <blockquote><pre>
 *      Object qname1 = compiler.qname(null, "foo")
 *      Object expr1 = compiler.number("1");
 *      Object expr2 = compiler.number("2");
 *      Object expr3 = compiler.div(expr1, expr2);
 *      Object expr4 = compiler.
 *              coreFunction(Compiler.FUNCTION_ROUND, new Object[]{expr3});
 *      Object test1 = compiler.nodeNameTest(qname1);
 *      Object step1 = compiler.
 *              step(Compiler.AXIS_CHILD, test1, new Object[]{expr4});
 *      Object test2 = compiler.nodeTypeTest(Compiler.NODE_TYPE_TEXT);
 *      Object step2 = compiler.nodeTypeTest(Compiler.AXIS_CHILD, test2, null);
 *      Object expr5 = compiler.locationPath(false, new Object[]{step1, step2});
 * </pre></blockquote>
 */
public interface Compiler {

    int NODE_TYPE_NODE = 1;
    int NODE_TYPE_TEXT = 2;
    int NODE_TYPE_COMMENT = 3;
    int NODE_TYPE_PI = 4;

    int AXIS_SELF = 1;
    int AXIS_CHILD = 2;
    int AXIS_PARENT = 3;
    int AXIS_ANCESTOR = 4;
    int AXIS_ATTRIBUTE = 5;
    int AXIS_NAMESPACE = 6;
    int AXIS_PRECEDING = 7;
    int AXIS_FOLLOWING = 8;
    int AXIS_DESCENDANT = 9;
    int AXIS_ANCESTOR_OR_SELF = 10;
    int AXIS_FOLLOWING_SIBLING = 11;
    int AXIS_PRECEDING_SIBLING = 12;
    int AXIS_DESCENDANT_OR_SELF = 13;

    int FUNCTION_LAST = 1;
    int FUNCTION_POSITION = 2;
    int FUNCTION_COUNT = 3;
    int FUNCTION_ID = 4;
    int FUNCTION_LOCAL_NAME = 5;
    int FUNCTION_NAMESPACE_URI = 6;
    int FUNCTION_NAME = 7;
    int FUNCTION_STRING = 8;
    int FUNCTION_CONCAT = 9;
    int FUNCTION_STARTS_WITH = 10;
    int FUNCTION_CONTAINS = 11;
    int FUNCTION_SUBSTRING_BEFORE = 12;
    int FUNCTION_SUBSTRING_AFTER = 13;
    int FUNCTION_SUBSTRING = 14;
    int FUNCTION_STRING_LENGTH = 15;
    int FUNCTION_NORMALIZE_SPACE = 16;
    int FUNCTION_TRANSLATE = 17;
    int FUNCTION_BOOLEAN = 18;
    int FUNCTION_NOT = 19;
    int FUNCTION_TRUE = 20;
    int FUNCTION_FALSE = 21;
    int FUNCTION_LANG = 22;
    int FUNCTION_NUMBER = 23;
    int FUNCTION_SUM = 24;
    int FUNCTION_FLOOR = 25;
    int FUNCTION_CEILING = 26;
    int FUNCTION_ROUND = 27;
    int FUNCTION_NULL = 28;
    int FUNCTION_KEY = 29;
    int FUNCTION_FORMAT_NUMBER = 30;

    int FUNCTION_ENDS_WITH = 31;

    /**
     * Produces an EXPRESSION object that represents a numeric constant.
     * @param value numeric String
     * @return Object
     */
    Object number(String value);

    /**
     * Produces an EXPRESSION object that represents a string constant.
     * @param value String literal
     * @return Object
     */
    Object literal(String value);

    /**
     * Produces an QNAME that represents a name with an optional prefix.
     * @param prefix String prefix
     * @param name String name
     * @return Object
     */
    Object qname(String prefix, String name);

    /**
     * Produces an EXPRESSION object representing the sum of all argumens
     *
     * @param arguments are EXPRESSION objects
     * @return Object
     */
    Object sum(Object[] arguments);

    /**
     * Produces an EXPRESSION object representing <em>left</em> minus <em>right</em>
     *
     * @param left is an EXPRESSION object
     * @param right is an EXPRESSION object
     * @return Object
     */
    Object minus(Object left, Object right);

    /**
     * Produces  an EXPRESSION object representing <em>left</em> multiplied by
     * <em>right</em>
     *
     * @param left is an EXPRESSION object
     * @param right is an EXPRESSION object
     * @return Object
     */
    Object multiply(Object left, Object right);

    /**
     * Produces  an EXPRESSION object representing <em>left</em> divided by
     * <em>right</em>
     *
     * @param left is an EXPRESSION object
     * @param right is an EXPRESSION object
     * @return Object
     */
    Object divide(Object left, Object right);

    /**
     * Produces  an EXPRESSION object representing <em>left</em> modulo
     * <em>right</em>
     *
     * @param left is an EXPRESSION object
     * @param right is an EXPRESSION object
     * @return Object
     */
    Object mod(Object left, Object right);

    /**
     * Produces an EXPRESSION object representing the comparison:
     * <em>left</em> less than <em>right</em>
     *
     * @param left is an EXPRESSION object
     * @param right is an EXPRESSION object
     * @return Object
     */
    Object lessThan(Object left, Object right);

    /**
     * Produces an EXPRESSION object representing the comparison:
     * <em>left</em> less than or equal to <em>right</em>
     *
     * @param left is an EXPRESSION object
     * @param right is an EXPRESSION object
     * @return Object
     */
    Object lessThanOrEqual(Object left, Object right);

    /**
     * Produces an EXPRESSION object representing the comparison:
     * <em>left</em> greater than <em>right</em>
     *
     * @param left is an EXPRESSION object
     * @param right is an EXPRESSION object
     * @return Object
     */
    Object greaterThan(Object left, Object right);

    /**
     * Produces an EXPRESSION object representing the comparison:
     * <em>left</em> greater than or equal to <em>right</em>
     *
     * @param left is an EXPRESSION object
     * @param right is an EXPRESSION object
     * @return Object
     */
    Object greaterThanOrEqual(Object left, Object right);

    /**
     * Produces an EXPRESSION object representing the comparison:
     * <em>left</em> equals to <em>right</em>
     *
     * @param left is an EXPRESSION object
     * @param right is an EXPRESSION object
     * @return Object
     */
    Object equal(Object left, Object right);

    /**
     * Produces an EXPRESSION object representing the comparison:
     * <em>left</em> is not equal to <em>right</em>
     *
     * @param left is an EXPRESSION object
     * @param right is an EXPRESSION object
     * @return Object
     */
    Object notEqual(Object left, Object right);

    /**
     * Produces an EXPRESSION object representing unary negation of the argument
     *
     * @param argument is an EXPRESSION object
     * @return Object
     */
    Object minus(Object argument);

    /**
     * Produces an EXPRESSION object representing variable reference
     *
     * @param qname is a QNAME object
     * @return Object
     */
    Object variableReference(Object qname);

    /**
     * Produces an EXPRESSION object representing the computation of
     * a core function with the supplied arguments.
     *
     * @param code is one of FUNCTION_... constants
     * @param args are EXPRESSION objects
     * @return Object
     */
    Object function(int code, Object[] args);

    /**
     * Produces an EXPRESSION object representing the computation of
     * a library function with the supplied arguments.
     *
     * @param name is a QNAME object (function name)
     * @param args are EXPRESSION objects
     * @return Object
     */
    Object function(Object name, Object[] args);

    /**
     * Produces an EXPRESSION object representing logical conjunction of
     * all arguments
     *
     * @param arguments are EXPRESSION objects
     * @return Object
     */
    Object and(Object[] arguments);

    /**
     * Produces an EXPRESSION object representing logical disjunction of
     * all arguments
     *
     * @param arguments are EXPRESSION objects
     * @return Object
     */
    Object or(Object[] arguments);

    /**
     * Produces an EXPRESSION object representing union of all node sets
     *
     * @param arguments are EXPRESSION objects
     * @return Object
     */
    Object union(Object[] arguments);

    /**
     * Produces a NODE_TEST object that represents a node name test.
     *
     * @param qname is a QNAME object
     * @return Object
     */
    Object nodeNameTest(Object qname);

    /**
     * Produces a NODE_TEST object that represents a node type test.
     *
     * @param nodeType is a NODE_TEST object
     * @return Object
     */
    Object nodeTypeTest(int nodeType);

    /**
     * Produces  a NODE_TEST object that represents a processing instruction
     * test.
     *
     * @param instruction is a NODE_TEST object
     * @return Object
     */
    Object processingInstructionTest(String instruction);

    /**
     * Produces a STEP object that represents a node test.
     *
     * @param axis is one of the AXIS_... constants
     * @param nodeTest is a NODE_TEST object
     * @param predicates are EXPRESSION objects
     * @return Object
     */
    Object step(int axis, Object nodeTest, Object[] predicates);

    /**
     * Produces an EXPRESSION object representing a location path
     *
     * @param absolute indicates whether the path is absolute
     * @param steps are STEP objects
     * @return Object
     */
    Object locationPath(boolean absolute, Object[] steps);

    /**
     * Produces an EXPRESSION object representing a filter expression
     *
     * @param expression is an EXPRESSION object
     * @param predicates are EXPRESSION objects
     * @param steps are STEP objects
     * @return Object
     */
    Object expressionPath(
        Object expression,
        Object[] predicates,
        Object[] steps);
}
