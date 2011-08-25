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
/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - adjustment to EObject
 ******************************************************************************/
package org.eclipse.e4.emf.internal.xpath.helper;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.jxpath.Container;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.util.TypeUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Collection and property access utilities.
 */
public class ValueUtils {

	public static Object getValue(EObject bean, EStructuralFeature pd) {
		return bean.eGet(pd);
	}

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
        if (value instanceof Collection) {
            return true;
        }
        return false;
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
            return ((Collection<?>) collection).size();
        }
        return 1;
    }
    
    /**
     * Remove the index'th element from the supplied collection.
     * @param collection to edit
     * @param index int
     * @return the resulting collection
     */
    public static Object remove(Object collection, int index) {
        collection = getValue(collection);
        if (collection == null) {
            return null;
        }
        if (index >= getLength(collection)) {
            throw new JXPathException("No such element at index " + index);
        }
        if (collection.getClass().isArray()) {
            int length = Array.getLength(collection);
            Object smaller =
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
            int size = ((List<?>) collection).size();
            if (index < size) {
                ((List<?>) collection).remove(index);
            }
            return collection;
        }
        if (collection instanceof Collection) {
            Iterator<?> it = ((Collection<?>) collection).iterator();
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

	@SuppressWarnings("unchecked")
	public static Object getValue(EObject bean, EStructuralFeature pd, int index) {
		if (pd.isMany()) {
            try {
            	return ((List<Object>)bean.eGet(pd)).get(index);
            }
            catch (IndexOutOfBoundsException ex) {
                return null;
            }
            catch (Throwable ex) {
                throw new JXPathException(
                    "Cannot access property: " + pd.getName(),
                    ex);
            }
        }

        // We will fall through if there is no indexed read

        return getValue(getValue(bean, pd), index);
	}
	
	@SuppressWarnings("unchecked")
	public static Object getValue(Object collection, int index) {
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
                if (index < 0 || index >= ((List<?>) collection).size()) {
                    return null;
                }
                value = ((List<Object>) collection).get(index);
            }
            else if (collection instanceof Collection) {
                int i = 0;
                Iterator<Object> it = ((Collection<Object>) collection).iterator();
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
	
	public static int getCollectionHint(Class<?> clazz) {
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

	@SuppressWarnings("unchecked")
	public static void setValue(EObject bean, EStructuralFeature pd,
			Object value) {
		try {
			if( pd.isMany() ) {
				List<Object> l = (List<Object>) bean.eGet(pd);
				l.clear();
				l.addAll((Collection<Object>)value);
			} else {
				bean.eSet(pd, value);	
			}
        }
        catch (Exception ex) {
            throw new JXPathException(
                "Cannot modify property: "
                    + (bean == null ? "null" : bean.getClass().getName())
                    + "."
                    + pd.getName(),
                ex);
        }
	}

	public static void setValue(EObject bean, EStructuralFeature pd, int index,
			Object value) {
		if (pd.isMany()) {
            try {
            	@SuppressWarnings("unchecked")
				List<Object> l = (List<Object>) bean.eGet(pd);
            	l.set(index, convert(value, pd.getEType().getInstanceClass()));
            }
            catch (Exception ex) {
                throw new RuntimeException(
                    "Cannot access property: "
                        + pd.getName()
                        + ", "
                        + ex.getMessage());
            }
        }
        // We will fall through if there is no indexed read
        Object collection = getValue(bean, pd);
        if (isCollection(collection)) {
            setValue(collection, index, value);
        }
        else if (index == 0) {
            setValue(bean, pd, value);
        }
        else {
            throw new RuntimeException(
                "Not a collection: " + pd.getName());
        }
	}
	
	@SuppressWarnings("unchecked")
	public static void setValue(Object collection, int index, Object value) {
        collection = getValue(collection);
        if (collection != null) {
            if (collection.getClass().isArray()) {
                Array.set(
                    collection,
                    index,
                    convert(value, collection.getClass().getComponentType()));
            }
            else if (collection instanceof List) {
                ((List<Object>) collection).set(index, value);
            }
            else if (collection instanceof Collection) {
                throw new UnsupportedOperationException(
                        "Cannot set value of an element of a "
                                + collection.getClass().getName());
            }
        }
    }
	
	private static Object convert(Object value, Class<?> type) {
        try {
            return TypeUtils.convert(value, type);
        }
        catch (Exception ex) {
            throw new JXPathException(
                "Cannot convert value of class "
                    + (value == null ? "null" : value.getClass().getName())
                    + " to type "
                    + type,
                ex);
        }
    }
	
	public static Object expandCollection(Object collection, int size) {
        if (collection == null) {
            return null;
        }
        if (size < getLength(collection)) {
            throw new JXPathException("adjustment of " + collection
                    + " to size " + size + " is not an expansion");
        }
        if (collection.getClass().isArray()) {
            Object bigger =
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
        	@SuppressWarnings("unchecked")
			Collection<Object> c = (Collection<Object>) collection; 
            while (c.size() < size) {
                c.add(null);
            }
            return collection;
        }
        throw new JXPathException(
            "Cannot turn "
                + collection.getClass().getName()
                + " into a collection of size "
                + size);
    }
}
