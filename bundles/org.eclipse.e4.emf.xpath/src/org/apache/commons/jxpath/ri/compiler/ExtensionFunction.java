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

import java.util.Arrays;

import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.JXPathFunctionNotFoundException;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.axes.NodeSetContext;

/**
 * Represents an element of the parse tree representing an extension function
 * call.
 */
public class ExtensionFunction extends Operation {

    private final QName functionName;

    /**
     * Create a new ExtensionFunction.
     * @param functionName name of the function
     * @param args Expression[] of function args
     */
    public ExtensionFunction(final QName functionName, final Expression[] args) {
        super(args);
        this.functionName = functionName;
    }

    /**
     * Gets the function name
     * @return QName
     */
    public QName getFunctionName() {
        return functionName;
    }

    /**
     * An extension function gets the current context, therefore it MAY be
     * context dependent.
     * @return true
     */
    @Override
    public boolean computeContextDependent() {
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(functionName);
        buffer.append('(');
        final Expression[] args = getArguments();
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    buffer.append(", ");
                }
                buffer.append(args[i]);
            }
        }
        buffer.append(')');
        return buffer.toString();
    }

    @Override
    public Object compute(final EvalContext context) {
        return computeValue(context);
    }

    @Override
    public Object computeValue(final EvalContext context) {
        Object[] parameters = null;
        if (args != null) {
            parameters = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                parameters[i] = convert(args[i].compute(context));
            }
        }

        final Function function =
            context.getRootContext().getFunction(functionName, parameters);
        if (function == null) {
            throw new JXPathFunctionNotFoundException("No such function: "
                    + functionName + Arrays.asList(parameters));
        }
        final Object result = function.invoke(context, parameters);
        return result instanceof NodeSet ? new NodeSetContext(context,
                (NodeSet) result) : result;
    }

    /**
     * Convert any incoming context to a value.
     * @param object Object to convert
     * @return context value or {@code object} unscathed.
     */
    private Object convert(final Object object) {
        return object instanceof EvalContext ? ((EvalContext) object).getValue() : object;
    }
}
