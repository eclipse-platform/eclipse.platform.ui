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
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - adjustment to EObject
 ******************************************************************************/
package org.eclipse.e4.emf.internal.xpath;

import java.util.Objects;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.eclipse.e4.emf.internal.xpath.helper.ValueUtils;
import org.eclipse.emf.ecore.EObject;

/**
 * A pointer allocated by a PropertyOwnerPointer to represent the value of
 * a property of the parent object.
 *
 */
public abstract class EStructuralFeaturePointer extends NodePointer {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static final int UNSPECIFIED_PROPERTY = Integer.MIN_VALUE;

	/** property index */
	protected int propertyIndex = UNSPECIFIED_PROPERTY;

	/** owning object */
	protected Object bean;

	/**
	 * Takes a javabean, a descriptor of a property of that object and
	 * an offset within that property (starting with 0).
	 * @param parent parent pointer
	 */
	public EStructuralFeaturePointer(NodePointer parent) {
		super(parent);
	}

	/**
	 * Get the property index.
	 * @return int index
	 */
	public int getPropertyIndex() {
		return propertyIndex;
	}

	/**
	 * Set the property index.
	 * @param index property index
	 */
	public void setPropertyIndex(int index) {
		if (propertyIndex != index) {
			propertyIndex = index;
			setIndex(WHOLE_COLLECTION);
		}
	}

	/**
	 * Get the parent bean.
	 * @return Object
	 */
	public EObject getBean() {
		if (bean == null) {
			bean = getImmediateParentPointer().getNode();
		}
		return (EObject) bean;
	}

	@Override
	public QName getName() {
		return new QName(null, getPropertyName());
	}

	/**
	 * Get the property name.
	 * @return String property name.
	 */
	public abstract String getPropertyName();

	/**
	 * Set the property name.
	 * @param propertyName property name to set.
	 */
	public abstract void setPropertyName(String propertyName);

	/**
	 * Count the number of properties represented.
	 * @return int
	 */
	public abstract int getPropertyCount();

	/**
	 * Get the names of the included properties.
	 * @return String[]
	 */
	public abstract String[] getPropertyNames();

	/**
	 * Learn whether this pointer references an actual property.
	 * @return true if actual
	 */
	protected abstract boolean isActualProperty();

	@Override
	public boolean isActual() {
		if (!isActualProperty()) {
			return false;
		}

		return super.isActual();
	}

	private static final Object UNINITIALIZED = new Object();

	private Object value = UNINITIALIZED;

	@Override
	public Object getImmediateNode() {
		if (value == UNINITIALIZED) {
			value = index == WHOLE_COLLECTION ? ValueUtils.getValue(getBaseValue())
					: ValueUtils.getValue(getBaseValue(), index);
		}
		return value;
	}

	@Override
	public boolean isCollection() {
		Object value = getBaseValue();
		return value != null && ValueUtils.isCollection(value);
	}

	@Override
	public boolean isLeaf() {
		Object value = getNode();
		return value == null || JXPathIntrospector.getBeanInfo(value.getClass()).isAtomic();
	}

	/**
	 * If the property contains a collection, then the length of that
	 * collection, otherwise - 1.
	 * @return int length
	 */
	@Override
	public int getLength() {
		return ValueUtils.getLength(getBaseValue());
	}

	/**
	 * Returns a NodePointer that can be used to access the currently
	 * selected property value.
	 * @return NodePointer
	 */
	@Override
	public NodePointer getImmediateValuePointer() {
		return NodePointer.newChildNodePointer(
			(NodePointer) this.clone(),
			getName(),
			getImmediateNode());
	}

	@Override
	public NodePointer createPath(JXPathContext context) {
		if (getImmediateNode() == null) {
			AbstractFactory factory = getAbstractFactory(context);
			int inx = (index == WHOLE_COLLECTION ? 0 : index);
			boolean success =
				factory.createObject(
					context,
					this,
					getBean(),
					getPropertyName(),
					inx);
			if (!success) {
				throw new JXPathAbstractFactoryException("Factory " + factory
						+ " could not create an object for path: " + asPath());
			}
		}
		return this;
	}

	@Override
	public NodePointer createPath(JXPathContext context, Object value) {
		// If neccessary, expand collection
		if (index != WHOLE_COLLECTION && index >= getLength()) {
			createPath(context);
		}
		setValue(value);
		return this;
	}

	@Override
	public NodePointer createChild(
		JXPathContext context,
		QName name,
		int index,
		Object value) {
		EStructuralFeaturePointer prop = (EStructuralFeaturePointer) clone();
		if (name != null) {
			prop.setPropertyName(name.toString());
		}
		prop.setIndex(index);
		return prop.createPath(context, value);
	}

	@Override
	public NodePointer createChild(
		JXPathContext context,
		QName name,
		int index) {
		EStructuralFeaturePointer prop = (EStructuralFeaturePointer) clone();
		if (name != null) {
			prop.setPropertyName(name.toString());
		}
		prop.setIndex(index);
		return prop.createPath(context);
	}

	@Override
	public int hashCode() {
		return getImmediateParentPointer().hashCode() + propertyIndex + index;
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}

		if (!(object instanceof EStructuralFeaturePointer)) {
			return false;
		}

		EStructuralFeaturePointer other = (EStructuralFeaturePointer) object;
		if (!Objects.equals(parent, other.parent)) {
			return false;
		}

		if (getPropertyIndex() != other.getPropertyIndex()
			|| !getPropertyName().equals(other.getPropertyName())) {
			return false;
		}

		int iThis = (index == WHOLE_COLLECTION ? 0 : index);
		int iOther = (other.index == WHOLE_COLLECTION ? 0 : other.index);
		return iThis == iOther;
	}

	@Override
	public int compareChildNodePointers(
		NodePointer pointer1,
		NodePointer pointer2) {
		return getValuePointer().compareChildNodePointers(pointer1, pointer2);
	}

}
