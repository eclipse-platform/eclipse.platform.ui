/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 171616
 *******************************************************************************/

package org.eclipse.core.internal.databinding.internal.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.core.runtime.Assert;

/**
 * @since 1.0
 * 
 */
public class JavaBeanObservableSet extends ObservableSet implements IBeanObservable {

	private final Object object;

	private PropertyChangeListener collectionListener = new PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (!updating) {
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
	};

	private boolean updating = false;

	private PropertyDescriptor descriptor;

	private ListenerSupport collectionListenSupport;

	private boolean attachListeners;

	/**
	 * @param realm
	 * @param object
	 * @param descriptor
	 * @param elementType
	 */
	public JavaBeanObservableSet(Realm realm, Object object,
			PropertyDescriptor descriptor, Class elementType) {
		this(realm, object, descriptor, elementType, true);
	}

	/**
	 * @param realm
	 * @param object
	 * @param descriptor
	 * @param elementType
	 * @param attachListeners
	 */
	public JavaBeanObservableSet(Realm realm, Object object,
			PropertyDescriptor descriptor, Class elementType,
			boolean attachListeners) {
		super(realm, new HashSet(), elementType);
		this.object = object;
		this.descriptor = descriptor;
		this.attachListeners = attachListeners;
		if (attachListeners) {
			this.collectionListenSupport = new ListenerSupport(
					collectionListener, descriptor.getName());
		}

		wrappedSet.addAll(Arrays.asList(getValues()));
	}

	protected void firstListenerAdded() {
		if (attachListeners) {
			collectionListenSupport.hookListener(this.object);
		}
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

	/* (non-Javadoc)
	 * @see org.eclipse.core.databinding.beans.IBeanObservable#getObserved()
	 */
	public Object getObserved() {
		return object;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.databinding.beans.IBeanObservable#getPropertyDescriptor()
	 */
	public PropertyDescriptor getPropertyDescriptor() {
		return descriptor;
	}

}
