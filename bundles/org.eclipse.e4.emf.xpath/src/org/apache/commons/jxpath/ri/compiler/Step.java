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

/**
 */
public class Step {
    private final int axis;
    private final NodeTest nodeTest;
    private final Expression[] predicates;

    /**
     * Create a new Step.
     * @param axis axis code
     * @param nodeTest step test
     * @param predicates predicate expressions
     */
    protected Step(final int axis, final NodeTest nodeTest, final Expression[] predicates) {
        this.axis = axis;
        this.nodeTest = nodeTest;
        this.predicates = predicates;
    }

    /**
     * Gets the axis code.
     * @return int
     */
    public int getAxis() {
        return axis;
    }

    /**
     * Gets the step test.
     * @return NodeTest
     */
    public NodeTest getNodeTest() {
        return nodeTest;
    }

    /**
     * Gets the predicates.
     * @return Expression[]
     */
    public Expression[] getPredicates() {
        return predicates;
    }

    /**
     * Learn whether this step contains any predicate that is context dependent.
     * @return boolean
     */
    public boolean isContextDependent() {
        if (predicates != null) {
            for (final Expression predicate : predicates) {
                if (predicate.isContextDependent()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        final int axis = getAxis();
        if (axis == Compiler.AXIS_CHILD) {
            buffer.append(nodeTest);
        }
        else if (axis == Compiler.AXIS_ATTRIBUTE) {
            buffer.append('@');
            buffer.append(nodeTest);
        }
        else if (axis == Compiler.AXIS_SELF
                && nodeTest instanceof NodeTypeTest
                && ((NodeTypeTest) nodeTest).getNodeType()
                    == Compiler.NODE_TYPE_NODE) {
            buffer.append(".");
        }
        else if (axis == Compiler.AXIS_PARENT
                && nodeTest instanceof NodeTypeTest
                && ((NodeTypeTest) nodeTest).getNodeType()
                    == Compiler.NODE_TYPE_NODE) {
            buffer.append("..");
        }
        else if (axis == Compiler.AXIS_DESCENDANT_OR_SELF
                && nodeTest instanceof NodeTypeTest
                && ((NodeTypeTest) nodeTest).getNodeType()
                    == Compiler.NODE_TYPE_NODE
                && (predicates == null || predicates.length == 0)) {
            buffer.append("");
        }
        else {
            buffer.append(axisToString(axis));
            buffer.append("::");
            buffer.append(nodeTest);
        }
        final Expression[] predicates = getPredicates();
        if (predicates != null) {
            for (final Expression predicate : predicates) {
                buffer.append('[');
                buffer.append(predicate);
                buffer.append(']');
            }
        }
        return buffer.toString();
    }

    /**
     * Decode an axis code to its name.
     * @param axis int code
     * @return String name.
     * @see Compiler
     * @see "http://www.w3.org/TR/xpath#axes"
     */
    public static String axisToString(final int axis) {
        switch (axis) {
            case Compiler.AXIS_SELF :
                return "self";
            case Compiler.AXIS_CHILD :
                return "child";
            case Compiler.AXIS_PARENT :
                return "parent";
            case Compiler.AXIS_ANCESTOR :
                return "ancestor";
            case Compiler.AXIS_ATTRIBUTE :
                return "attribute";
            case Compiler.AXIS_NAMESPACE :
                return "namespace";
            case Compiler.AXIS_PRECEDING :
                return "preceding";
            case Compiler.AXIS_FOLLOWING :
                return "following";
            case Compiler.AXIS_DESCENDANT :
                return "descendant";
            case Compiler.AXIS_ANCESTOR_OR_SELF :
                return "ancestor-or-self";
            case Compiler.AXIS_FOLLOWING_SIBLING :
                return "following-sibling";
            case Compiler.AXIS_PRECEDING_SIBLING :
                return "preceding-sibling";
            case Compiler.AXIS_DESCENDANT_OR_SELF :
                return "descendant-or-self";
            default:
                return "UNKNOWN";
        }
    }
}
