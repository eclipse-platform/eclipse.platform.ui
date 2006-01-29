/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.databinding.AbstractUpdatableSet;
import org.eclipse.jface.databinding.IReadableSet;
import org.eclipse.jface.util.Assert;

/**
 * @since 3.2
 * 
 */
public class JavaBeansReadableSet extends AbstractUpdatableSet implements
		IReadableSet {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.databinding.AbstractUpdatableSet#computeElements()
	 */
	protected Collection computeElements() {
		return Collections.unmodifiableSet(elements);
	}

	private final Object object;

	private Set elements;

	private PropertyChangeListener collectionListener = new PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (!updating) {
				if (JavaBeansReadableSet.this.descriptor.getName().equals(
						event.getPropertyName())) {
					Set newElements = new HashSet(Arrays.asList(getValues()));
					Set addedElements = new HashSet(newElements);
					Set removedElements = new HashSet(elements);
					// remove all new elements from old elements to compute the
					// removed elements
					removedElements.removeAll(newElements);
					addedElements.removeAll(elements);
					elements.addAll(addedElements);
					fireAdded(addedElements);
					elements = newElements;
					fireRemoved(removedElements);
				}
			}
		}
	};

	private boolean updating = false;

	private PropertyDescriptor descriptor;

	private Class elementType = null;

	private ListenerSupport collectionListenSupport = new ListenerSupport(
			collectionListener);

	/**
	 * @param object
	 * @param descriptor
	 */
	public JavaBeansReadableSet(Object object, PropertyDescriptor descriptor,
			Class elementType) {
		this.object = object;
		this.descriptor = descriptor;
		this.elementType = elementType;
		collectionListenSupport.hookListener(this.object);
		elements = new HashSet(Arrays.asList(getValues()));
	}

	public void dispose() {
		super.dispose();
		collectionListenSupport.dispose();
	}

	private Object primGetValues() {
		try {
			Method readMethod = descriptor.getReadMethod();
			if (!readMethod.isAccessible()) {
				readMethod.setAccessible(true);
			}
			return readMethod.invoke(object, new Object[0]);
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		Assert.isTrue(false, "Could not read collection values"); //$NON-NLS-1$
		return null;
	}

	private Object[] getValues() {
		Object[] values = null;

		Object result = primGetValues();
		if (descriptor.getPropertyType().isArray())
			values = (Object[]) result;
		else {
			// TODO add jUnit for POJO (var. SettableValue) collections
			Collection list = (Collection) result;
			if (list != null) {
				values = list.toArray();
			} else {
				values = new Object[] {};
			}
		}
		return values;
	}

}
