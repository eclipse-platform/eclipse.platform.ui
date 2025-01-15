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
package org.apache.commons.jxpath;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.jxpath.functions.ConstructorFunction;
import org.apache.commons.jxpath.functions.MethodFunction;
import org.apache.commons.jxpath.util.MethodLookupUtils;

/**
 * Extension functions provided by a Java class.
 *
 * Let's say we declared a ClassFunction like this:
 * <blockquote><pre>
 *     new ClassFunctions(Integer.class, "int")
 * </pre></blockquote>
 *
 * We can now use XPaths like:
 * <dl>
 *  <dt>{@code "int:new(3)"}</dt>
 *  <dd>Equivalent to {@code new Integer(3)}</dd>
 *  <dt>{@code "int:getInteger('foo')"}</dt>
 *  <dd>Equivalent to {@code Integer.getInteger("foo")}</dd>
 *  <dt>{@code "int:floatValue(int:new(4))"}</dt>
 *  <dd>Equivalent to {@code new Integer(4).floatValue()}</dd>
 * </dl>
 *
 * <p>
 * If the first argument of a method is {@link ExpressionContext}, the
 * expression context in which the function is evaluated is passed to
 * the method.
 */
public class ClassFunctions implements Functions {
    private static final Object[] EMPTY_ARRAY = {};

    private final Class functionClass;
    private final String namespace;

    /**
     * Create a new ClassFunctions.
     * @param functionClass Class providing the functions
     * @param namespace assigned ns
     */
    public ClassFunctions(final Class functionClass, final String namespace) {
        this.functionClass = functionClass;
        this.namespace = namespace;
    }

    /**
     * Returns a set of one namespace - the one specified in the constructor.
     *
     * @return a singleton
     */
    @Override
    public Set getUsedNamespaces() {
        return Collections.singleton(namespace);
    }

    /**
     * Returns a {@link Function}, if any, for the specified namespace,
     * name and parameter types.
     *
     * @param namespace if it is not the namespace specified in the constructor,
     *     the method returns null
     * @param name is a function name or "new" for a constructor.
     * @param parameters Object[] of parameters
     * @return a MethodFunction, a ConstructorFunction or null if there is no
     *      such function.
     */
    @Override
    public Function getFunction(
        final String namespace,
        final String name,
        Object[] parameters) {
        if (namespace == null) {
            if (this.namespace != null) {
                return null;
            }
        }
        else if (!namespace.equals(this.namespace)) {
            return null;
        }

        if (parameters == null) {
            parameters = EMPTY_ARRAY;
        }

        if (name.equals("new")) {
            final Constructor constructor =
                MethodLookupUtils.lookupConstructor(functionClass, parameters);
            if (constructor != null) {
                return new ConstructorFunction(constructor);
            }
        }
        else {
            Method method = MethodLookupUtils.
                lookupStaticMethod(functionClass, name, parameters);
            if (method != null) {
                return new MethodFunction(method);
            }

            method = MethodLookupUtils.
                lookupMethod(functionClass, name, parameters);
            if (method != null) {
                return new MethodFunction(method);
            }
        }

        return null;
    }
}
