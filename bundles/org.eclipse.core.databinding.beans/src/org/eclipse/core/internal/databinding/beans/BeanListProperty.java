/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222, 264307
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.list.SimpleListProperty;

/**
 * @since 3.3
 * 
 */
public class BeanListProperty extends SimpleListProperty {
	private final PropertyDescriptor propertyDescriptor;
	private final Class elementType;

	/**
	 * @param propertyDescriptor
	 * @param elementType
	 */
	public BeanListProperty(PropertyDescriptor propertyDescriptor,
			Class elementType) {
		this.propertyDescriptor = propertyDescriptor;
		this.elementType = elementType == null ? BeanPropertyHelper
				.getCollectionPropertyElementType(propertyDescriptor)
				: elementType;
	}

	public Object getElementType() {
		return elementType;
	}

	protected List doGetList(Object source) {
		return asList(BeanPropertyHelper.readProperty(source,
				propertyDescriptor));
	}

	private List asList(Object propertyValue) {
		if (propertyValue == null)
			return new ArrayList();
		if (propertyDescriptor.getPropertyType().isArray())
			return new ArrayList(Arrays.asList((Object[]) propertyValue));
		return (List) propertyValue;
	}

	protected void doSetList(Object source, List list, ListDiff diff) {
		BeanPropertyHelper.writeProperty(source, propertyDescriptor,
				convertListToBeanPropertyType(list));
	}

	private Object convertListToBeanPropertyType(List list) {
		Object propertyValue = list;
		if (propertyDescriptor.getPropertyType().isArray()) {
			Class componentType = propertyDescriptor.getPropertyType()
					.getComponentType();
			Object[] array = (Object[]) Array.newInstance(componentType, list
					.size());
			list.toArray(array);
			propertyValue = array;
		}
		return propertyValue;
	}

	public INativePropertyListener adaptListener(
			final ISimplePropertyListener listener) {
		return new Listener(listener);
	}

	private class Listener implements INativePropertyListener,
			PropertyChangeListener {
		private final ISimplePropertyListener listener;

		private Listener(ISimplePropertyListener listener) {
			this.listener = listener;
		}

		public void propertyChange(java.beans.PropertyChangeEvent evt) {
			if (propertyDescriptor.getName().equals(evt.getPropertyName())) {
				ListDiff diff;
				Object oldValue = evt.getOldValue();
				Object newValue = evt.getNewValue();
				if (oldValue != null && newValue != null) {
					diff = Diffs.computeListDiff(asList(oldValue),
							asList(newValue));
				} else {
					diff = null;
				}
				listener.handlePropertyChange(new SimplePropertyEvent(evt
						.getSource(), BeanListProperty.this, diff));
			}
		}
	}

	protected void doAddListener(Object source, INativePropertyListener listener) {
		BeanPropertyListenerSupport.hookListener(source, propertyDescriptor
				.getName(), (PropertyChangeListener) listener);
	}

	protected void doRemoveListener(Object source,
			INativePropertyListener listener) {
		BeanPropertyListenerSupport.unhookListener(source, propertyDescriptor
				.getName(), (PropertyChangeListener) listener);
	}

	public String toString() {
		String s = BeanPropertyHelper.propertyName(propertyDescriptor) + "[]"; //$NON-NLS-1$
		if (elementType != null)
			s += "<" + BeanPropertyHelper.shortClassName(elementType) + ">"; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}
}
