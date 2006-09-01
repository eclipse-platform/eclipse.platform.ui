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

package org.eclipse.jface.internal.databinding.internal.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.databinding.observable.Diffs;
import org.eclipse.jface.databinding.observable.set.ObservableSet;
import org.eclipse.jface.util.Assert;

/**
 * @since 1.0
 * 
 */
public class JavaBeanObservableSet extends ObservableSet {

	private final Object object;

	private PropertyChangeListener collectionListener = new PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (!updating) {
				if (JavaBeanObservableSet.this.descriptor.getName().equals(
						event.getPropertyName())) {
					Set newElements = new HashSet(Arrays.asList(getValues()));
					Set addedElements = new HashSet(newElements);
					Set removedElements = new HashSet(wrappedSet);
					// remove all new elements from old elements to compute the
					// removed elements
					removedElements.removeAll(newElements);
					addedElements.removeAll(wrappedSet);
					wrappedSet = newElements;
					fireSetChange(Diffs.createSetDiff(addedElements,
							removedElements));
				}
			}
		}
	};

	private boolean updating = false;

	private PropertyDescriptor descriptor;

	private ListenerSupport collectionListenSupport = new ListenerSupport(
			collectionListener);

	/**
	 * @param object
	 * @param descriptor
	 * @param elementType
	 */
	public JavaBeanObservableSet(Object object, PropertyDescriptor descriptor,
			Class elementType) {
		super(new HashSet(), descriptor.getPropertyType());
		this.object = object;
		this.descriptor = descriptor;
		wrappedSet.addAll(Arrays.asList(getValues()));
	}

	protected void firstListenerAdded() {
		collectionListenSupport.hookListener(this.object);
	}

	protected void lastListenerRemoved() {
		if (collectionListenSupport != null) {
			collectionListenSupport.dispose();
		}
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
