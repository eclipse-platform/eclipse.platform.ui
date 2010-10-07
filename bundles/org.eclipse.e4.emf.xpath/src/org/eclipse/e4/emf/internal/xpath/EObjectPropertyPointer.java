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
package org.eclipse.e4.emf.internal.xpath;

import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.eclipse.e4.emf.internal.xpath.helper.JXPathEObjectInfo;
import org.eclipse.e4.emf.internal.xpath.helper.ValueUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Pointer pointing to a property of a {@link EObject}.
 */
public class EObjectPropertyPointer extends EStructuralFeaturePointer {
    private static final long serialVersionUID = -6008991447676468786L;

    private static final Object UNINITIALIZED = new Object();

    private String propertyName;
    private final JXPathEObjectInfo beanInfo;
    private Object baseValue = UNINITIALIZED;
    private Object value = UNINITIALIZED;
    private transient String[] names;
    private transient EStructuralFeature[] propertyDescriptors;
    private transient EStructuralFeature propertyDescriptor;

    /**
     * Create a new BeanPropertyPointer.
     * @param parent parent pointer
     * @param beanInfo describes the target property/ies.
     */
    public EObjectPropertyPointer(NodePointer parent, JXPathEObjectInfo beanInfo) {
        super(parent);
        this.beanInfo = beanInfo;
    }

    /**
     * This type of node is auxiliary.
     * @return true
     */
    public boolean isContainer() {
        return true;
    }

    public int getPropertyCount() {
        if (beanInfo.isAtomic()) {
            return 0;
        }
        return getPropertyDescriptors().length;
    }

    /**
     * Get the names of all properties, sorted alphabetically
     * @return String[]
     */
    public String[] getPropertyNames() {
        if (names == null) {
            EStructuralFeature[] pds = getPropertyDescriptors();
            names = new String[pds.length];
            for (int i = 0; i < names.length; i++) {
                names[i] = pds[i].getName();
            }
        }
        return names;
    }

    /**
     * Select a property by name.
     * @param propertyName String name
     */
    public void setPropertyName(String propertyName) {
        setPropertyIndex(UNSPECIFIED_PROPERTY);
        this.propertyName = propertyName;
    }

    /**
     * Selects a property by its offset in the alphabetically sorted list.
     * @param index property index
     */
    public void setPropertyIndex(int index) {
        if (propertyIndex != index) {
            super.setPropertyIndex(index);
            propertyName = null;
            propertyDescriptor = null;
            baseValue = UNINITIALIZED;
            value = UNINITIALIZED;
        }
    }

    /**
     * Get the value of the currently selected property.
     * @return Object value
     */
    public Object getBaseValue() {
        if (baseValue == UNINITIALIZED) {
            EStructuralFeature pd = getPropertyDescriptor();
            if (pd == null) {
                return null;
            }
            baseValue = ValueUtils.getValue(getBean(), pd);
        }
        return baseValue;
    }

    public void setIndex(int index) {
        if (this.index == index) {
            return;
        }
        // When dealing with a scalar, index == 0 is equivalent to
        // WHOLE_COLLECTION, so do not change it.
        if (this.index != WHOLE_COLLECTION
                || index != 0
                || isCollection()) {
            super.setIndex(index);
            value = UNINITIALIZED;
        }
    }

    /**
     * If index == WHOLE_COLLECTION, the value of the property, otherwise
     * the value of the index'th element of the collection represented by the
     * property. If the property is not a collection, index should be zero
     * and the value will be the property itself.
     * @return Object
     */
    public Object getImmediateNode() {
        if (value == UNINITIALIZED) {
            if (index == WHOLE_COLLECTION) {
                value = ValueUtils.getValue(getBaseValue());
            }
            else {
                EStructuralFeature pd = getPropertyDescriptor();
                if (pd == null) {
                    value = null;
                }
                else {
                    value = ValueUtils.getValue(getBean(), pd, index);
                }
            }
        }
        return value;
    }

    protected boolean isActualProperty() {
        return getPropertyDescriptor() != null;
    }

    public boolean isCollection() {
        EStructuralFeature pd = getPropertyDescriptor();
        
        if (pd == null) {
            return false;
        }
        
        if( pd.isMany() ) {
        	return true;
        }

//        if (pd instanceof IndexedPropertyDescriptor) {
//            return true;
//        }
//
        int hint = ValueUtils.getCollectionHint(pd.getEType().getInstanceClass());
        if (hint == -1) {
            return false;
        }
        if (hint == 1) {
            return true;
        }

        Object value = getBaseValue();
        return value != null && ValueUtils.isCollection(value);
    }

    /**
     * If the property contains a collection, then the length of that
     * collection, otherwise - 1.
     * @return int length
     */
    public int getLength() {
        EStructuralFeature pd = getPropertyDescriptor();
        if (pd == null) {
            return 1;
        }
        
        if( pd.isMany() ) {
        	return ((List<?>)getBean().eGet(pd)).size();
        }

//        if (pd instanceof IndexedPropertyDescriptor) {
//            return ValueUtils.getIndexedPropertyLength(
//                getBean(),
//                (IndexedPropertyDescriptor) pd);
//        }
//
//        int hint = ValueUtils.getCollectionHint(pd.getPropertyType());
//        if (hint == -1) {
//            return 1;
//        }
        return ValueUtils.getLength(getBaseValue());
    }

    /**
     * If index == WHOLE_COLLECTION, change the value of the property, otherwise
     * change the value of the index'th element of the collection
     * represented by the property.
     * @param value value to set
     */
    public void setValue(Object value) {
        EStructuralFeature pd = getPropertyDescriptor();
        if (pd == null) {
            throw new JXPathInvalidAccessException(
                "Cannot set property: " + asPath() + " - no such property");
        }

        if (index == WHOLE_COLLECTION) {
            ValueUtils.setValue(getBean(), pd, value);
        }
        else {
            ValueUtils.setValue(getBean(), pd, index, value);
        }
        this.value = value;
    }

    public NodePointer createPath(JXPathContext context) {
        if (getImmediateNode() == null) {
            super.createPath(context);
            baseValue = UNINITIALIZED;
            value = UNINITIALIZED;
        }
        return this;
    }

    public void remove() {
        if (index == WHOLE_COLLECTION) {
            setValue(null);
        }
        else if (isCollection()) {
            Object o = getBaseValue();
            Object collection = ValueUtils.remove(getBaseValue(), index);
            if (collection != o) {
                ValueUtils.setValue(getBean(), getPropertyDescriptor(), collection);
            }
        }
        else if (index == 0) {
            index = WHOLE_COLLECTION;
            setValue(null);
        }
    }

    /**
     * Get the name of the currently selected property.
     * @return String property name
     */
    public String getPropertyName() {
        if (propertyName == null) {
            EStructuralFeature pd = getPropertyDescriptor();
            if (pd != null) {
                propertyName = pd.getName();
            }
        }
        return propertyName != null ? propertyName : "*";
    }

    /**
     * Finds the property descriptor corresponding to the current property
     * index.
     * @return PropertyDescriptor
     */
    private EStructuralFeature getPropertyDescriptor() {
        if (propertyDescriptor == null) {
            int inx = getPropertyIndex();
            if (inx == UNSPECIFIED_PROPERTY) {
                propertyDescriptor =
                    beanInfo.getPropertyDescriptor(propertyName);
            }
            else {
                EStructuralFeature[] propertyDescriptors =
                    getPropertyDescriptors();
                if (inx >= 0 && inx < propertyDescriptors.length) {
                    propertyDescriptor = propertyDescriptors[inx];
                }
                else {
                    propertyDescriptor = null;
                }
            }
        }
        return propertyDescriptor;
    }

    /**
     * Get all PropertyDescriptors.
     * @return PropertyDescriptor[]
     */
    protected synchronized EStructuralFeature[] getPropertyDescriptors() {
        if (propertyDescriptors == null) {
            propertyDescriptors = beanInfo.getPropertyDescriptors();
        }
        return propertyDescriptors;
    }
}
