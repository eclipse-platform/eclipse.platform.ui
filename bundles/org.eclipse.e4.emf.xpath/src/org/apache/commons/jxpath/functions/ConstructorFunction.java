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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.apache.commons.jxpath.util.TypeUtils;

/**
 * An extension function that creates an instance using a constructor.
 */
public class ConstructorFunction implements Function {
    private static final Object[] EMPTY_ARRAY = {};

    private final Constructor constructor;

    /**
     * Create a new ConstructorFunction.
     * @param constructor the constructor to call.
     */
    public ConstructorFunction(final Constructor constructor) {
        this.constructor = constructor;
    }

    /**
     * Converts parameters to suitable types and invokes the constructor.
     * @param context evaluation context
     * @param parameters constructor args
     * @return new instance
     */
    @Override
    public Object invoke(final ExpressionContext context, Object[] parameters) {
        try {
            Object[] args;
            if (parameters == null) {
                parameters = EMPTY_ARRAY;
            }
            int pi = 0;
            final Class[] types = constructor.getParameterTypes();
            if (types.length > 0
                && ExpressionContext.class.isAssignableFrom(types[0])) {
                pi = 1;
            }
            args = new Object[parameters.length + pi];
            if (pi == 1) {
                args[0] = context;
            }
            for (int i = 0; i < parameters.length; i++) {
                args[i + pi] = TypeUtils.convert(parameters[i], types[i + pi]);
            }
            return constructor.newInstance(args);
        }
        catch (Throwable ex) {
            if (ex instanceof InvocationTargetException) {
                ex = ((InvocationTargetException) ex).getTargetException();
            }
            throw new JXPathInvalidAccessException(
                "Cannot invoke constructor " + constructor,
                ex);
        }
    }
}
