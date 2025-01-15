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
package org.apache.commons.jxpath.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.JXPathException;

/**
 * Method lookup utilities, which find static and non-static methods as well
 * as constructors based on a name and list of parameters.
 */
public class MethodLookupUtils {

    private static final int NO_MATCH = 0;
    private static final int APPROXIMATE_MATCH = 1;
    private static final int EXACT_MATCH = 2;

    /**
     * Look up a constructor.
     * @param targetClass the class constructed
     * @param parameters arguments
     * @return Constructor found if any.
     */
    public static Constructor lookupConstructor(
        final Class targetClass,
        final Object[] parameters) {
        boolean tryExact = true;
        final int count = parameters == null ? 0 : parameters.length;
        final Class[] types = new Class[count];
        for (int i = 0; i < count; i++) {
            final Object param = parameters[i];
            if (param != null) {
                types[i] = param.getClass();
            }
            else {
                types[i] = null;
                tryExact = false;
            }
        }

        Constructor constructor = null;

        if (tryExact) {
            // First - without type conversion
            try {
                constructor = targetClass.getConstructor(types);
                if (constructor != null) {
                    return constructor;
                }
            }
            catch (final NoSuchMethodException ignore) { // NOPMD
                // Ignore
            }
        }

        int currentMatch = 0;
        boolean ambiguous = false;

        // Then - with type conversion
        final Constructor[] constructors = targetClass.getConstructors();
        for (final Constructor constructor2 : constructors) {
            final int match =
                matchParameterTypes(
                    constructor2.getParameterTypes(),
                    parameters);
            if (match != NO_MATCH) {
                if (match > currentMatch) {
                    constructor = constructor2;
                    currentMatch = match;
                    ambiguous = false;
                }
                else if (match == currentMatch) {
                    ambiguous = true;
                }
            }
        }
        if (ambiguous) {
            throw new JXPathException(
                "Ambiguous constructor " + Arrays.asList(parameters));
        }
        return constructor;
    }

    /**
     * Look up a static method.
     * @param targetClass the owning class
     * @param name method name
     * @param parameters method parameters
     * @return Method found if any
     */
    public static Method lookupStaticMethod(
        final Class targetClass,
        final String name,
        final Object[] parameters) {
        boolean tryExact = true;
        final int count = parameters == null ? 0 : parameters.length;
        final Class[] types = new Class[count];
        for (int i = 0; i < count; i++) {
            final Object param = parameters[i];
            if (param != null) {
                types[i] = param.getClass();
            }
            else {
                types[i] = null;
                tryExact = false;
            }
        }

        Method method = null;

        if (tryExact) {
            // First - without type conversion
            try {
                method = targetClass.getMethod(name, types);
                if (method != null
                    && Modifier.isStatic(method.getModifiers())) {
                    return method;
                }
            }
            catch (final NoSuchMethodException ignore) { // NOPMD
                // Ignore
            }
        }

        int currentMatch = 0;
        boolean ambiguous = false;

        // Then - with type conversion
        final Method[] methods = targetClass.getMethods();
        for (final Method method2 : methods) {
            if (Modifier.isStatic(method2.getModifiers())
                && method2.getName().equals(name)) {
                final int match =
                    matchParameterTypes(
                        method2.getParameterTypes(),
                        parameters);
                if (match != NO_MATCH) {
                    if (match > currentMatch) {
                        method = method2;
                        currentMatch = match;
                        ambiguous = false;
                    }
                    else if (match == currentMatch) {
                        ambiguous = true;
                    }
                }
            }
        }
        if (ambiguous) {
            throw new JXPathException("Ambiguous method call: " + name);
        }
        return method;
    }

    /**
     * Look up a method.
     * @param targetClass owning class
     * @param name method name
     * @param parameters method parameters
     * @return Method found if any
     */
    public static Method lookupMethod(
        Class targetClass,
        final String name,
        final Object[] parameters) {
        if (parameters == null
            || parameters.length < 1
            || parameters[0] == null) {
            return null;
        }

        if (matchType(targetClass, parameters[0]) == NO_MATCH) {
            return null;
        }

        targetClass = TypeUtils.convert(parameters[0], targetClass).getClass();

        boolean tryExact = true;
        final int count = parameters.length - 1;
        final Class[] types = new Class[count];
        final Object[] arguments = new Object[count];
        for (int i = 0; i < count; i++) {
            final Object param = parameters[i + 1];
            arguments[i] = param;
            if (param != null) {
                types[i] = param.getClass();
            }
            else {
                types[i] = null;
                tryExact = false;
            }
        }

        Method method = null;

        if (tryExact) {
            // First - without type conversion
            try {
                method = targetClass.getMethod(name, types);
                if (method != null
                    && !Modifier.isStatic(method.getModifiers())) {
                    return method;
                }
            }
            catch (final NoSuchMethodException ignore) { // NOPMD
                // Ignore
            }
        }

        int currentMatch = 0;
        boolean ambiguous = false;

        // Then - with type conversion
        final Method[] methods = targetClass.getMethods();
        for (final Method method2 : methods) {
            if (!Modifier.isStatic(method2.getModifiers())
                && method2.getName().equals(name)) {
                final int match =
                    matchParameterTypes(
                        method2.getParameterTypes(),
                        arguments);
                if (match != NO_MATCH) {
                    if (match > currentMatch) {
                        method = method2;
                        currentMatch = match;
                        ambiguous = false;
                    }
                    else if (match == currentMatch) {
                        ambiguous = true;
                    }
                }
            }
        }
        if (ambiguous) {
            throw new JXPathException("Ambiguous method call: " + name);
        }
        return method;
    }

    /**
     * Return a match code of objects to types.
     * @param types Class[] of expected types
     * @param parameters Object[] to attempt to match
     * @return int code
     */
    private static int matchParameterTypes(
        final Class[] types,
        final Object[] parameters) {
        int pi = 0;
        if (types.length >= 1
            && ExpressionContext.class.isAssignableFrom(types[0])) {
            pi++;
        }
        final int length = parameters == null ? 0 : parameters.length;
        if (types.length != length + pi) {
            return NO_MATCH;
        }
        int totalMatch = EXACT_MATCH;
        for (int i = 0; i < length; i++) {
            final int match = matchType(types[i + pi], parameters[i]);
            if (match == NO_MATCH) {
                return NO_MATCH;
            }
            if (match < totalMatch) {
                totalMatch = match;
            }
        }
        return totalMatch;
    }

    /**
     * Return a match code between an object and type.
     * @param expected class to test
     * @param object object to test
     * @return int code
     */
    private static int matchType(final Class expected, final Object object) {
        if (object == null) {
            return APPROXIMATE_MATCH;
        }

        final Class actual = object.getClass();

        if (expected.equals(actual)) {
            return EXACT_MATCH;
        }
        if (expected.isAssignableFrom(actual)) {
            return EXACT_MATCH;
        }

        if (TypeUtils.canConvert(object, expected)) {
            return APPROXIMATE_MATCH;
        }

        return NO_MATCH;
    }
}
