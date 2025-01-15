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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.jxpath.functions.ConstructorFunction;
import org.apache.commons.jxpath.functions.MethodFunction;
import org.apache.commons.jxpath.util.ClassLoaderUtil;
import org.apache.commons.jxpath.util.MethodLookupUtils;
import org.apache.commons.jxpath.util.TypeUtils;

/**
 * Extension functions provided by Java classes.  The class prefix specified
 * in the constructor is used when a constructor or a static method is called.
 * Usually, a class prefix is a package name (hence the name of this class).
 *
 * Let's say, we declared a PackageFunction like this:
 * <blockquote><pre>
 *     new PackageFunctions("java.util.", "util")
 * </pre></blockquote>
 *
 * We can now use XPaths like:
 * <dl>
 *  <dt>{@code "util:Date.new()"}</dt>
 *  <dd>Equivalent to {@code new java.util.Date()}</dd>
 *  <dt>{@code "util:Collections.singleton('foo')"}</dt>
 *  <dd>Equivalent to {@code java.util.Collections.singleton("foo")}</dd>
 *  <dt>{@code "util:substring('foo', 1, 2)"}</dt>
 *  <dd>Equivalent to {@code "foo".substring(1, 2)}.  Note that in
 *  this case, the class prefix is not used. JXPath does not check that
 *  the first parameter of the function (the method target) is in fact
 *  a member of the package described by this PackageFunctions object.</dd>
 * </dl>
 *
 * <p>
 * If the first argument of a method or constructor is {@link ExpressionContext},
 * the expression context in which the function is evaluated is passed to
 * the method.
 * </p>
 * <p>
 * There is one PackageFunctions object registered by default with each
 * JXPathContext.  It does not have a namespace and uses no class prefix.
 * The existence of this object allows us to use XPaths like:
 * {@code "java.util.Date.new()"} and {@code "length('foo')"}
 * without the explicit registration of any extension functions.
 * </p>
 */
public class PackageFunctions implements Functions {
    private final String classPrefix;
    private final String namespace;
    private static final Object[] EMPTY_ARRAY = {};

    /**
     * Create a new PackageFunctions.
     * @param classPrefix class prefix
     * @param namespace namespace String
     */
    public PackageFunctions(final String classPrefix, final String namespace) {
        this.classPrefix = classPrefix;
        this.namespace = namespace;
    }

    /**
     * Returns the namespace specified in the constructor
     * @return (singleton) namespace Set
     */
    @Override
    public Set getUsedNamespaces() {
        return Collections.singleton(namespace);
    }

    /**
     * Returns a {@link Function}, if found, for the specified namespace,
     * name and parameter types.
     *
     * @param  namespace - if it is not the same as specified in the
     * construction, this method returns null
     * @param name - name of the method, which can one these forms:
     * <ul>
     * <li><strong>methodname</strong>, if invoking a method on an object passed as the
     * first parameter</li>
     * <li><strong>Classname.new</strong>, if looking for a constructor</li>
     * <li><strong>subpackage.subpackage.Classname.new</strong>, if looking for a
     * constructor in a subpackage</li>
     * <li><strong>Classname.methodname</strong>, if looking for a static method</li>
     * <li><strong>subpackage.subpackage.Classname.methodname</strong>, if looking for a
     * static method of a class in a subpackage</li>
     * </ul>
     * @param parameters Object[] of parameters
     * @return a MethodFunction, a ConstructorFunction or null if no function
     * is found
     */
    @Override
    public Function getFunction(
        final String namespace,
        final String name,
        Object[] parameters) {
        if (!Objects.equals(this.namespace, namespace)) {
            return null;
        }

        if (parameters == null) {
            parameters = EMPTY_ARRAY;
        }

        if (parameters.length >= 1) {
            Object target = TypeUtils.convert(parameters[0], Object.class);
            if (target != null) {
                Method method =
                    MethodLookupUtils.lookupMethod(
                        target.getClass(),
                        name,
                        parameters);
                if (method != null) {
                    return new MethodFunction(method);
                }

                if (target instanceof NodeSet) {
                    target = ((NodeSet) target).getPointers();
                }

                method =
                    MethodLookupUtils.lookupMethod(
                        target.getClass(),
                        name,
                        parameters);
                if (method != null) {
                    return new MethodFunction(method);
                }

                if (target instanceof Collection) {
                    final Iterator iter = ((Collection) target).iterator();
                    if (iter.hasNext()) {
                        target = iter.next();
                        if (target instanceof Pointer) {
                            target = ((Pointer) target).getValue();
                        }
                    }
                    else {
                        target = null;
                    }
                }
            }
            if (target != null) {
                final Method method =
                    MethodLookupUtils.lookupMethod(
                        target.getClass(),
                        name,
                        parameters);
                if (method != null) {
                    return new MethodFunction(method);
                }
            }
        }

        final String fullName = classPrefix + name;
        final int inx = fullName.lastIndexOf('.');
        if (inx == -1) {
            return null;
        }

        final String className = fullName.substring(0, inx);
        final String methodName = fullName.substring(inx + 1);

        Class functionClass;
        try {
            functionClass = ClassLoaderUtil.getClass(className, true);
        }
        catch (final ClassNotFoundException ex) {
            throw new JXPathException(
                "Cannot invoke extension function "
                    + (namespace != null ? namespace + ":" + name : name),
                ex);
        }

        if (methodName.equals("new")) {
            final Constructor constructor =
                MethodLookupUtils.lookupConstructor(functionClass, parameters);
            if (constructor != null) {
                return new ConstructorFunction(constructor);
            }
        }
        else {
            final Method method =
                MethodLookupUtils.lookupStaticMethod(
                    functionClass,
                    methodName,
                    parameters);
            if (method != null) {
                return new MethodFunction(method);
            }
        }
        return null;
    }
}
