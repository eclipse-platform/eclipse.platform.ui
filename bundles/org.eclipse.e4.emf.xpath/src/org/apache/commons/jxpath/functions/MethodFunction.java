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
package org.apache.commons.jxpath.functions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.apache.commons.jxpath.util.TypeUtils;
import org.apache.commons.jxpath.util.ValueUtils;

/**
 * An XPath extension function implemented as an individual Java method.
 */
public class MethodFunction implements Function {

    private final Method method;
    private static final Object[] EMPTY_ARRAY = {};

    /**
     * Create a new MethodFunction.
     * @param method implementing Method
     */
    public MethodFunction(final Method method) {
        this.method = ValueUtils.getAccessibleMethod(method);
    }

    @Override
    public Object invoke(final ExpressionContext context, Object[] parameters) {
        try {
            Object target;
            Object[] args;
            if (Modifier.isStatic(method.getModifiers())) {
                target = null;
                if (parameters == null) {
                    parameters = EMPTY_ARRAY;
                }
                int pi = 0;
                final Class[] types = method.getParameterTypes();
                if (types.length >= 1
                    && ExpressionContext.class.isAssignableFrom(types[0])) {
                    pi = 1;
                }
                args = new Object[parameters.length + pi];
                if (pi == 1) {
                    args[0] = context;
                }
                for (int i = 0; i < parameters.length; i++) {
                    args[i + pi] =
                        TypeUtils.convert(parameters[i], types[i + pi]);
                }
            }
            else {
                int pi = 0;
                final Class[] types = method.getParameterTypes();
                if (types.length >= 1
                    && ExpressionContext.class.isAssignableFrom(types[0])) {
                    pi = 1;
                }
                target =
                    TypeUtils.convert(
                        parameters[0],
                        method.getDeclaringClass());
                args = new Object[parameters.length - 1 + pi];
                if (pi == 1) {
                    args[0] = context;
                }
                for (int i = 1; i < parameters.length; i++) {
                    args[pi + i - 1] =
                        TypeUtils.convert(parameters[i], types[i + pi - 1]);
                }
            }

            return method.invoke(target, args);
        }
        catch (Throwable ex) {
            if (ex instanceof InvocationTargetException) {
                ex = ((InvocationTargetException) ex).getTargetException();
            }
            throw new JXPathInvalidAccessException("Cannot invoke " + method,
                    ex);
        }
    }

    @Override
    public String toString() {
        return method.toString();
    }
}
