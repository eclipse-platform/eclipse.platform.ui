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

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.apache.commons.jxpath.JXPathTypeConversionException;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.Pointer;

/**
 * The default implementation of TypeConverter.
 */
public class BasicTypeConverter implements TypeConverter {

    /**
     * Returns true if it can convert the supplied
     * object to the specified class.
     * @param object to check
     * @param toType prospective destination class
     * @return boolean
     */
    @Override
    public boolean canConvert(final Object object, final Class toType) {
        if (object == null) {
            return true;
        }
        final Class useType = TypeUtils.wrapPrimitive(toType);
        final Class fromType = object.getClass();

        if (useType.isAssignableFrom(fromType)) {
            return true;
        }

        if (useType == String.class) {
            return true;
        }

        if (object instanceof Boolean && (Number.class.isAssignableFrom(useType)
                || "java.util.concurrent.atomic.AtomicBoolean"
                        .equals(useType.getName()))) {
            return true;
        }
        if (object instanceof Number
                && (Number.class.isAssignableFrom(useType) || useType == Boolean.class)) {
            return true;
        }
        if (object instanceof String
                && (useType == Boolean.class
                        || useType == Character.class
                        || useType == Byte.class
                        || useType == Short.class
                        || useType == Integer.class
                        || useType == Long.class
                        || useType == Float.class
                        || useType == Double.class)) {
                return true;
        }
        if (fromType.isArray()) {
            // Collection -> array
            if (useType.isArray()) {
                final Class cType = useType.getComponentType();
                final int length = Array.getLength(object);
                for (int i = 0; i < length; i++) {
                    final Object value = Array.get(object, i);
                    if (!canConvert(value, cType)) {
                        return false;
                    }
                }
                return true;
            }
            if (Collection.class.isAssignableFrom(useType)) {
                return canCreateCollection(useType);
            }
            if (Array.getLength(object) > 0) {
                final Object value = Array.get(object, 0);
                return canConvert(value, useType);
            }
            return canConvert("", useType);
        }
        if (object instanceof Collection) {
            // Collection -> array
            if (useType.isArray()) {
                final Class cType = useType.getComponentType();
                final Iterator it = ((Collection) object).iterator();
                while (it.hasNext()) {
                    final Object value = it.next();
                    if (!canConvert(value, cType)) {
                        return false;
                    }
                }
                return true;
            }
            if (Collection.class.isAssignableFrom(useType)) {
                return canCreateCollection(useType);
            }
            if (((Collection) object).size() > 0) {
                Object value;
                if (object instanceof List) {
                    value = ((List) object).get(0);
                }
                else {
                    final Iterator it = ((Collection) object).iterator();
                    value = it.next();
                }
                return canConvert(value, useType);
            }
            return canConvert("", useType);
        }
        if (object instanceof NodeSet) {
            return canConvert(((NodeSet) object).getValues(), useType);
        }
        if (object instanceof Pointer) {
            return canConvert(((Pointer) object).getValue(), useType);
        }
        return ConvertUtils.lookup(useType) != null;
    }

    /**
     * Converts the supplied object to the specified
     * type. Throws a runtime exception if the conversion is
     * not possible.
     * @param object to convert
     * @param toType destination class
     * @return converted object
     */
    @Override
    public Object convert(final Object object, final Class toType) {
        if (object == null) {
            return toType.isPrimitive() ? convertNullToPrimitive(toType) : null;
        }

        if (toType == Object.class) {
            if (object instanceof NodeSet) {
                return convert(((NodeSet) object).getValues(), toType);
            }
            if (object instanceof Pointer) {
                return convert(((Pointer) object).getValue(), toType);
            }
            return object;
        }
        final Class useType = TypeUtils.wrapPrimitive(toType);
        final Class fromType = object.getClass();

        if (useType.isAssignableFrom(fromType)) {
            return object;
        }

        if (fromType.isArray()) {
            final int length = Array.getLength(object);
            if (useType.isArray()) {
                final Class cType = useType.getComponentType();

                final Object array = Array.newInstance(cType, length);
                for (int i = 0; i < length; i++) {
                    final Object value = Array.get(object, i);
                    Array.set(array, i, convert(value, cType));
                }
                return array;
            }
            if (Collection.class.isAssignableFrom(useType)) {
                final Collection collection = allocateCollection(useType);
                for (int i = 0; i < length; i++) {
                    collection.add(Array.get(object, i));
                }
                return unmodifiableCollection(collection);
            }
            if (length > 0) {
                final Object value = Array.get(object, 0);
                return convert(value, useType);
            }
            return convert("", useType);
        }
        if (object instanceof Collection) {
            final int length = ((Collection) object).size();
            if (useType.isArray()) {
                final Class cType = useType.getComponentType();
                final Object array = Array.newInstance(cType, length);
                final Iterator it = ((Collection) object).iterator();
                for (int i = 0; i < length; i++) {
                    final Object value = it.next();
                    Array.set(array, i, convert(value, cType));
                }
                return array;
            }
            if (Collection.class.isAssignableFrom(useType)) {
                final Collection collection = allocateCollection(useType);
                collection.addAll((Collection) object);
                return unmodifiableCollection(collection);
            }
            if (length > 0) {
                Object value;
                if (object instanceof List) {
                    value = ((List) object).get(0);
                }
                else {
                    final Iterator it = ((Collection) object).iterator();
                    value = it.next();
                }
                return convert(value, useType);
            }
            return convert("", useType);
        }
        if (object instanceof NodeSet) {
            return convert(((NodeSet) object).getValues(), useType);
        }
        if (object instanceof Pointer) {
            return convert(((Pointer) object).getValue(), useType);
        }
        if (useType == String.class) {
            return object.toString();
        }
        if (object instanceof Boolean) {
            if (Number.class.isAssignableFrom(useType)) {
                return allocateNumber(useType, ((Boolean) object).booleanValue() ? 1 : 0);
            }
            if ("java.util.concurrent.atomic.AtomicBoolean".equals(useType.getName())) {
                try {
                    return useType.getConstructor(new Class[] { boolean.class })
                            .newInstance(object);
                }
                catch (final Exception e) {
                    throw new JXPathTypeConversionException(useType.getName(), e);
                }
            }
        }
        if (object instanceof Number) {
            final double value = ((Number) object).doubleValue();
            if (useType == Boolean.class) {
                return value == 0.0 ? Boolean.FALSE : Boolean.TRUE;
            }
            if (Number.class.isAssignableFrom(useType)) {
                return allocateNumber(useType, value);
            }
        }
        if (object instanceof String) {
            final Object value = convertStringToPrimitive(object, useType);
            if (value != null) {
                return value;
            }
        }

        final Converter converter = ConvertUtils.lookup(useType);
        if (converter != null) {
            return converter.convert(useType, object);
        }

        throw new JXPathTypeConversionException("Cannot convert "
                + object.getClass() + " to " + useType);
    }

    /**
     * Convert null to a primitive type.
     * @param toType destination class
     * @return a wrapper
     */
    protected Object convertNullToPrimitive(final Class toType) {
        if (toType == boolean.class) {
            return Boolean.FALSE;
        }
        if (toType == char.class) {
            return Character.valueOf('\0');
        }
        if (toType == byte.class) {
            return Byte.valueOf((byte) 0);
        }
        if (toType == short.class) {
            return Short.valueOf((short) 0);
        }
        if (toType == int.class) {
            return Integer.valueOf(0);
        }
        if (toType == long.class) {
            return Long.valueOf(0L);
        }
        if (toType == float.class) {
            return Float.valueOf(0.0f);
        }
        if (toType == double.class) {
            return Double.valueOf(0.0);
        }
        return null;
    }

    /**
     * Convert a string to a primitive type.
     * @param object String
     * @param toType destination class
     * @return wrapper
     */
    protected Object convertStringToPrimitive(final Object object, Class toType) {
        toType = TypeUtils.wrapPrimitive(toType);
        if (toType == Boolean.class) {
            return Boolean.valueOf((String) object);
        }
        if (toType == Character.class) {
            return Character.valueOf(((String) object).charAt(0));
        }
        if (toType == Byte.class) {
            return Byte.valueOf((String) object);
        }
        if (toType == Short.class) {
            return Short.valueOf((String) object);
        }
        if (toType == Integer.class) {
            return Integer.valueOf((String) object);
        }
        if (toType == Long.class) {
            return Long.valueOf((String) object);
        }
        if (toType == Float.class) {
            return Float.valueOf((String) object);
        }
        if (toType == Double.class) {
            return Double.valueOf((String) object);
        }
        return null;
    }

    /**
     * Allocate a number of a given type and value.
     * @param type destination class
     * @param value double
     * @return Number
     */
    protected Number allocateNumber(Class type, final double value) {
        type = TypeUtils.wrapPrimitive(type);
        if (type == Byte.class) {
            return Byte.valueOf((byte) value);
        }
        if (type == Short.class) {
            return Short.valueOf((short) value);
        }
        if (type == Integer.class) {
            return Integer.valueOf((int) value);
        }
        if (type == Long.class) {
            return Long.valueOf((long) value);
        }
        if (type == Float.class) {
            return Float.valueOf((float) value);
        }
        if (type == Double.class) {
            return Double.valueOf(value);
        }
        if (type == BigInteger.class) {
            return BigInteger.valueOf((long) value);
        }
        if (type == BigDecimal.class) {
            // TODO ? https://pmd.sourceforge.io/pmd-6.50.0/pmd_rules_java_errorprone.html#avoiddecimalliteralsinbigdecimalconstructor
            return new BigDecimal(value); // NOPMD
        }
        final String className = type.getName();
        Class initialValueType = null;
        if ("java.util.concurrent.atomic.AtomicInteger".equals(className)) {
            initialValueType = int.class;
        }
        if ("java.util.concurrent.atomic.AtomicLong".equals(className)) {
            initialValueType = long.class;
        }
        if (initialValueType != null) {
            try {
                return (Number) type.getConstructor(
                        new Class[] { initialValueType })
                        .newInstance(
                                allocateNumber(initialValueType,
                                        value));
            }
            catch (final Exception e) {
                throw new JXPathTypeConversionException(className, e);
            }
        }
        return null;
    }

    /**
     * Learn whether this BasicTypeConverter can create a collection of the specified type.
     * @param type prospective destination class
     * @return boolean
     */
    protected boolean canCreateCollection(final Class type) {
        if (!type.isInterface()
                && (type.getModifiers() & Modifier.ABSTRACT) == 0) {
            try {
                type.getConstructor();
                return true;
            }
            catch (final Exception e) {
                return false;
            }
        }
        return type == List.class || type == Collection.class || type == Set.class;
    }

    /**
     * Create a collection of a given type.
     * @param type destination class
     * @return Collection
     */
    protected Collection allocateCollection(final Class type) {
        if (!type.isInterface()
                && (type.getModifiers() & Modifier.ABSTRACT) == 0) {
            try {
                return (Collection) type.getConstructor().newInstance();
            }
            catch (final Exception ex) {
                throw new JXPathInvalidAccessException(
                        "Cannot create collection of type: " + type, ex);
            }
        }

        if (type == List.class || type == Collection.class) {
            return new ArrayList();
        }
        if (type == Set.class) {
            return new HashSet();
        }
        throw new JXPathInvalidAccessException(
                "Cannot create collection of type: " + type);
    }

    /**
     * Gets an unmodifiable version of a collection.
     * @param collection to wrap
     * @return Collection
     */
    protected Collection unmodifiableCollection(final Collection collection) {
        if (collection instanceof List) {
            return Collections.unmodifiableList((List) collection);
        }
        if (collection instanceof SortedSet) {
            return Collections.unmodifiableSortedSet((SortedSet) collection);
        }
        if (collection instanceof Set) {
            return Collections.unmodifiableSet((Set) collection);
        }
        return Collections.unmodifiableCollection(collection);
    }

    /**
     * NodeSet implementation
     */
    static final class ValueNodeSet implements NodeSet {
        private final List values;
        private List pointers;

        /**
         * Create a new ValueNodeSet.
         * @param values to return
         */
        public ValueNodeSet(final List values) {
           this.values = values;
        }

        @Override
        public List getValues() {
            return Collections.unmodifiableList(values);
        }

        @Override
        public List getNodes() {
            return Collections.unmodifiableList(values);
        }

        @Override
        public List getPointers() {
            if (pointers == null) {
                pointers = new ArrayList();
                for (int i = 0; i < values.size(); i++) {
                    pointers.add(new ValuePointer(values.get(i)));
                }
                pointers = Collections.unmodifiableList(pointers);
            }
            return pointers;
        }
    }

    /**
     * Value pointer
     */
    static final class ValuePointer implements Pointer {
        private static final long serialVersionUID = -4817239482392206188L;

        private final Object bean;

        /**
         * Create a new ValuePointer.
         * @param object value
         */
        public ValuePointer(final Object object) {
            this.bean = object;
        }

        @Override
        public Object getValue() {
            return bean;
        }

        @Override
        public Object getNode() {
            return bean;
        }

        @Override
        public Object getRootNode() {
            return bean;
        }

        @Override
        public void setValue(final Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object clone() {
            return this;
        }

        @Override
        public int compareTo(final Object object) {
            return 0;
        }

        @Override
        public String asPath() {
            if (bean == null) {
                return "null()";
            }
            if (bean instanceof Number) {
                String string = bean.toString();
                if (string.endsWith(".0")) {
                    string = string.substring(0, string.length() - 2);
                }
                return string;
            }
            if (bean instanceof Boolean) {
                return ((Boolean) bean).booleanValue() ? "true()" : "false()";
            }
            if (bean instanceof String) {
                return "'" + bean + "'";
            }
            return "{object of type " + bean.getClass().getName() + "}";
        }
    }
}
