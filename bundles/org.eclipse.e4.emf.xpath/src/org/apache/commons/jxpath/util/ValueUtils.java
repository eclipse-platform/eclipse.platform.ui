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

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.Container;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathException;

/**
 * Collection and property access utilities.
 */
public class ValueUtils {
    private static Map dynamicPropertyHandlerMap = new HashMap();
    private static final int UNKNOWN_LENGTH_MAX_COUNT = 16000;

    /**
     * Returns true if the object is an array or a Collection.
     * @param value to test
     * @return boolean
     */
    public static boolean isCollection(Object value) {
        value = getValue(value);
        if (value == null) {
            return false;
        }
        if (value.getClass().isArray()) {
            return true;
        }
        return value instanceof Collection;
    }

    /**
     * Returns 1 if the type is a collection,
     * -1 if it is definitely not
     * and 0 if it may be a collection in some cases.
     * @param clazz to test
     * @return int
     */
    public static int getCollectionHint(final Class clazz) {
        if (clazz.isArray()) {
            return 1;
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            return 1;
        }

        if (clazz.isPrimitive()) {
            return -1;
        }

        if (clazz.isInterface()) {
            return 0;
        }

        if (Modifier.isFinal(clazz.getModifiers())) {
            return -1;
        }

        return 0;
    }

    /**
     * If there is a regular non-indexed read method for this property,
     * uses this method to obtain the collection and then returns its
     * length.
     * Otherwise, attempts to guess the length of the collection by
     * calling the indexed get method repeatedly.  The method is supposed
     * to throw an exception if the index is out of bounds.
     * @param object collection
     * @param pd IndexedPropertyDescriptor
     * @return int
     */
    public static int getIndexedPropertyLength(final Object object,
            final IndexedPropertyDescriptor pd) {
        if (pd.getReadMethod() != null) {
            return getLength(getValue(object, pd));
        }

        final Method readMethod = pd.getIndexedReadMethod();
        if (readMethod == null) {
            throw new JXPathException(
                "No indexed read method for property " + pd.getName());
        }

        for (int i = 0; i < UNKNOWN_LENGTH_MAX_COUNT; i++) {
            try {
                readMethod.invoke(object, Integer.valueOf(i));
            }
            catch (final Throwable t) {
                return i;
            }
        }

        throw new JXPathException(
            "Cannot determine the length of the indexed property "
                + pd.getName());
    }

    /**
     * Returns the length of the supplied collection. If the supplied object
     * is not a collection, returns 1. If collection is null, returns 0.
     * @param collection to check
     * @return int
     */
    public static int getLength(Object collection) {
        if (collection == null) {
            return 0;
        }
        collection = getValue(collection);
        if (collection.getClass().isArray()) {
            return Array.getLength(collection);
        }
        if (collection instanceof Collection) {
            return ((Collection) collection).size();
        }
        return 1;
    }

    /**
     * Returns an iterator for the supplied collection. If the argument
     * is null, returns an empty iterator. If the argument is not
     * a collection, returns an iterator that produces just that one object.
     * @param collection to iterate
     * @return Iterator
     */
    public static Iterator iterate(final Object collection) {
        if (collection == null) {
            return Collections.EMPTY_LIST.iterator();
        }
        if (collection.getClass().isArray()) {
            final int length = Array.getLength(collection);
            if (length == 0) {
                return Collections.EMPTY_LIST.iterator();
            }
            final ArrayList list = new ArrayList();
            for (int i = 0; i < length; i++) {
                list.add(Array.get(collection, i));
            }
            return list.iterator();
        }
        if (collection instanceof Collection) {
            return ((Collection) collection).iterator();
        }
        return Collections.singletonList(collection).iterator();
    }

    /**
     * Grows the collection if necessary to the specified size. Returns
     * the new, expanded collection.
     * @param collection to expand
     * @param size desired size
     * @return collection or array
     */
    public static Object expandCollection(final Object collection, final int size) {
        if (collection == null) {
            return null;
        }
        if (size < getLength(collection)) {
            throw new JXPathException("adjustment of " + collection
                    + " to size " + size + " is not an expansion");
        }
        if (collection.getClass().isArray()) {
            final Object bigger =
                Array.newInstance(
                    collection.getClass().getComponentType(),
                    size);
            System.arraycopy(
                collection,
                0,
                bigger,
                0,
                Array.getLength(collection));
            return bigger;
        }
        if (collection instanceof Collection) {
            while (((Collection) collection).size() < size) {
                ((Collection) collection).add(null);
            }
            return collection;
        }
        throw new JXPathException(
            "Cannot turn "
                + collection.getClass().getName()
                + " into a collection of size "
                + size);
    }

    /**
     * Remove the index'th element from the supplied collection.
     * @param collection to edit
     * @param index int
     * @return the resulting collection
     */
    public static Object remove(Object collection, final int index) {
        collection = getValue(collection);
        if (collection == null) {
            return null;
        }
        if (index >= getLength(collection)) {
            throw new JXPathException("No such element at index " + index);
        }
        if (collection.getClass().isArray()) {
            final int length = Array.getLength(collection);
            final Object smaller =
                Array.newInstance(
                    collection.getClass().getComponentType(),
                    length - 1);
            if (index > 0) {
                System.arraycopy(collection, 0, smaller, 0, index);
            }
            if (index < length - 1) {
                System.arraycopy(
                    collection,
                    index + 1,
                    smaller,
                    index,
                    length - index - 1);
            }
            return smaller;
        }
        if (collection instanceof List) {
            final int size = ((List) collection).size();
            if (index < size) {
                ((List) collection).remove(index);
            }
            return collection;
        }
        if (collection instanceof Collection) {
            final Iterator it = ((Collection) collection).iterator();
            for (int i = 0; i < index; i++) {
                if (!it.hasNext()) {
                    break;
                }
                it.next();
            }
            if (it.hasNext()) {
                it.next();
                it.remove();
            }
            return collection;
        }
        throw new JXPathException(
            "Cannot remove "
                + collection.getClass().getName()
                + "["
                + index
                + "]");
    }

    /**
     * Returns the index'th element of the supplied collection.
     * @param collection to read
     * @param index int
     * @return collection[index]
     */
    public static Object getValue(Object collection, final int index) {
        collection = getValue(collection);
        Object value = collection;
        if (collection != null) {
            if (collection.getClass().isArray()) {
                if (index < 0 || index >= Array.getLength(collection)) {
                    return null;
                }
                value = Array.get(collection, index);
            }
            else if (collection instanceof List) {
                if (index < 0 || index >= ((List) collection).size()) {
                    return null;
                }
                value = ((List) collection).get(index);
            }
            else if (collection instanceof Collection) {
                if (index < 0 || index >= ((Collection) collection).size()) {
                    return null;
                }

                int i = 0;
                final Iterator it = ((Collection) collection).iterator();
                for (; i < index; i++) {
                    it.next();
                }
                if (it.hasNext()) {
                    value = it.next();
                }
                else {
                    value = null;
                }
            }
        }
        return value;
    }

    /**
     * Modifies the index'th element of the supplied collection.
     * Converts the value to the required type if necessary.
     * @param collection to edit
     * @param index to replace
     * @param value new value
     */
    public static void setValue(Object collection, final int index, final Object value) {
        collection = getValue(collection);
        if (collection != null) {
            if (collection.getClass().isArray()) {
                Array.set(
                    collection,
                    index,
                    convert(value, collection.getClass().getComponentType()));
            }
            else if (collection instanceof List) {
                ((List) collection).set(index, value);
            }
            else if (collection instanceof Collection) {
                throw new UnsupportedOperationException(
                        "Cannot set value of an element of a "
                                + collection.getClass().getName());
            }
        }
    }

    /**
     * Returns the value of the bean's property represented by
     * the supplied property descriptor.
     * @param bean to read
     * @param propertyDescriptor indicating what to read
     * @return Object value
     */
    public static Object getValue(final Object bean,
            final PropertyDescriptor propertyDescriptor) {
        Object value;
        try {
            final Method method =
                getAccessibleMethod(propertyDescriptor.getReadMethod());
            if (method == null) {
                throw new JXPathException("No read method");
            }
            value = method.invoke(bean);
        }
        catch (final Exception ex) {
            throw new JXPathException(
                "Cannot access property: "
                    + (bean == null ? "null" : bean.getClass().getName())
                    + "."
                    + propertyDescriptor.getName(),
                ex);
        }
        return value;
    }

    /**
     * Modifies the value of the bean's property represented by
     * the supplied property descriptor.
     * @param bean to read
     * @param propertyDescriptor indicating what to read
     * @param value to set
     */
    public static void setValue(final Object bean,
            final PropertyDescriptor propertyDescriptor, Object value) {
        try {
            final Method method =
                getAccessibleMethod(propertyDescriptor.getWriteMethod());
            if (method == null) {
                throw new JXPathException("No write method");
            }
            value = convert(value, propertyDescriptor.getPropertyType());
            method.invoke(bean, value);
        }
        catch (final Exception ex) {
            throw new JXPathException(
                "Cannot modify property: "
                    + (bean == null ? "null" : bean.getClass().getName())
                    + "."
                    + propertyDescriptor.getName(),
                ex);
        }
    }

    /**
     * Convert value to type.
     * @param value Object
     * @param type destination
     * @return conversion result
     */
    private static Object convert(final Object value, final Class type) {
        try {
            return TypeUtils.convert(value, type);
        }
        catch (final Exception ex) {
            throw new JXPathException(
                "Cannot convert value of class "
                    + (value == null ? "null" : value.getClass().getName())
                    + " to type "
                    + type,
                ex);
        }
    }

    /**
     * Returns the index'th element of the bean's property represented by
     * the supplied property descriptor.
     * @param bean to read
     * @param propertyDescriptor indicating what to read
     * @param index int
     * @return Object
     */
    public static Object getValue(final Object bean,
            final PropertyDescriptor propertyDescriptor, final int index) {
        if (propertyDescriptor instanceof IndexedPropertyDescriptor) {
            try {
                final IndexedPropertyDescriptor ipd =
                    (IndexedPropertyDescriptor) propertyDescriptor;
                final Method method = ipd.getIndexedReadMethod();
                if (method != null) {
                    return method.invoke(
                        bean,
                            Integer.valueOf(index));
                }
            }
            catch (final InvocationTargetException ex) {
                final Throwable t = ex.getTargetException();
                if (t instanceof IndexOutOfBoundsException) {
                    return null;
                }
                throw new JXPathException(
                    "Cannot access property: " + propertyDescriptor.getName(),
                    t);
            }
            catch (final Throwable ex) {
                throw new JXPathException(
                    "Cannot access property: " + propertyDescriptor.getName(),
                    ex);
            }
        }

        // We will fall through if there is no indexed read

        return getValue(getValue(bean, propertyDescriptor), index);
    }

    /**
     * Modifies the index'th element of the bean's property represented by
     * the supplied property descriptor. Converts the value to the required
     * type if necessary.
     * @param bean to edit
     * @param propertyDescriptor indicating what to set
     * @param index int
     * @param value to set
     */
    public static void setValue(final Object bean,
            final PropertyDescriptor propertyDescriptor, final int index, final Object value) {
        if (propertyDescriptor instanceof IndexedPropertyDescriptor) {
            try {
                final IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) propertyDescriptor;
                final Method method = ipd.getIndexedWriteMethod();
                if (method != null) {
                    method.invoke(bean, Integer.valueOf(index), convert(value, ipd.getIndexedPropertyType()));
                    return;
                }
            }
            catch (final Exception ex) {
                throw new IllegalArgumentException("Cannot access property: " + propertyDescriptor.getName() + ", " + ex.getMessage());
            }
        }
        // We will fall through if there is no indexed read
        final Object collection = getValue(bean, propertyDescriptor);
        if (isCollection(collection)) {
            setValue(collection, index, value);
        }
        else if (index == 0) {
            setValue(bean, propertyDescriptor, value);
        }
        else {
            throw new IllegalArgumentException("Not a collection: " + propertyDescriptor.getName());
        }
    }

    /**
     * If the parameter is a container, opens the container and
     * return the contents.  The method is recursive.
     * @param object to read
     * @return Object
     */
    public static Object getValue(Object object) {
        while (object instanceof Container) {
            object = ((Container) object).getValue();
        }
        return object;
    }

    /**
     * Returns a shared instance of the dynamic property handler class
     * returned by {@code getDynamicPropertyHandlerClass()}.
     * @param clazz to handle
     * @return DynamicPropertyHandler
     */
    public static DynamicPropertyHandler getDynamicPropertyHandler(final Class clazz) {
        return (DynamicPropertyHandler) dynamicPropertyHandlerMap.computeIfAbsent(clazz, k -> {
            try {
                return (DynamicPropertyHandler) clazz.getConstructor().newInstance();
            }
            catch (final Exception ex) {
                throw new JXPathException("Cannot allocate dynamic property handler of class " + clazz.getName(), ex);
            }
        });
    }

    //
    //  The rest of the code in this file was copied FROM
    //  org.apache.commons.beanutils.PropertyUtil. We don't want to introduce
    //  a dependency on BeanUtils yet - DP.
    //

    /**
     * Gets an accessible method (that is, one that can be invoked via
     * reflection) that implements the specified Method.  If no such method
     * can be found, return {@code null}.
     *
     * @param method The method that we wish to call
     * @return Method
     */
    public static Method getAccessibleMethod(final Method method) {

        // Make sure we have a method to check
        if (method == null) {
            return null;
        }

        // If the requested method is not public we cannot call it
        if (!Modifier.isPublic(method.getModifiers())) {
            return null;
        }

        // If the declaring class is public, we are done
        Class clazz = method.getDeclaringClass();
        if (Modifier.isPublic(clazz.getModifiers())) {
            return method;
        }

        final String name = method.getName();
        final Class[] parameterTypes = method.getParameterTypes();
        while (clazz != null) {
            // Check the implemented interfaces and subinterfaces
            final Method aMethod = getAccessibleMethodFromInterfaceNest(clazz,
                    name, parameterTypes);
            if (aMethod != null) {
                return aMethod;
            }

            clazz = clazz.getSuperclass();
            if (clazz != null && Modifier.isPublic(clazz.getModifiers())) {
                try {
                    return clazz.getDeclaredMethod(name, parameterTypes);
                }
                catch (final NoSuchMethodException ignore) { // NOPMD
                    //ignore
                }
            }
        }
        return null;
    }

    /**
     * Gets an accessible method (that is, one that can be invoked via
     * reflection) that implements the specified method, by scanning through
     * all implemented interfaces and subinterfaces.  If no such Method
     * can be found, return {@code null}.
     *
     * @param clazz Parent class for the interfaces to be checked
     * @param methodName Method name of the method we wish to call
     * @param parameterTypes The parameter type signatures
     * @return Method
     */
    private static Method getAccessibleMethodFromInterfaceNest(final Class clazz,
            final String methodName, final Class[] parameterTypes) {

        Method method = null;

        // Check the implemented interfaces of the parent class
        final Class[] interfaces = clazz.getInterfaces();
        for (final Class element : interfaces) {

            // Is this interface public?
            if (!Modifier.isPublic(element.getModifiers())) {
                continue;
            }

            // Does the method exist on this interface?
            try {
                method =
                    element.getDeclaredMethod(methodName, parameterTypes);
            }
            catch (final NoSuchMethodException ignore) { // NOPMD
                // ignore
            }
            if (method != null) {
                break;
            }

            // Recursively check our parent interfaces
            method =
                getAccessibleMethodFromInterfaceNest(
                    element,
                    methodName,
                    parameterTypes);
            if (method != null) {
                break;
            }
        }

        // Return whatever we have found
        return method;
    }
}
