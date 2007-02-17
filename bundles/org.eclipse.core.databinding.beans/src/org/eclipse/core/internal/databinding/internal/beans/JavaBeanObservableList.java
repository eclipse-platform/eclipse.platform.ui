/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 171616
 ******************************************************************************/

package org.eclipse.core.internal.databinding.internal.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.runtime.Assert;

/**
 * @since 1.0
 * 
 */
public class JavaBeanObservableList extends ObservableList implements IBeanObservable {

	private final Object object;

	private PropertyChangeListener collectionListener = new PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (!updating) {
				updateWrappedList(Arrays.asList(getValues()));
			}
		}
	};

	private boolean updating = false;

	private PropertyDescriptor descriptor;

	private ListenerSupport collectionListenSupport;

	/**
	 * @param realm
	 * @param object
	 * @param descriptor
	 * @param elementType
	 */
	public JavaBeanObservableList(Realm realm, Object object, PropertyDescriptor descriptor,
			Class elementType) {
		super(realm, new ArrayList(), elementType);
		this.object = object;
		this.descriptor = descriptor;
		this.collectionListenSupport = new ListenerSupport(collectionListener,
				descriptor.getName());

		// initialize list without firing events
		wrappedList.addAll(Arrays.asList(getValues()));
	}

	protected void firstListenerAdded() {
		collectionListenSupport.hookListener(this.object);
	}

	protected void lastListenerRemoved() {
		if (collectionListenSupport != null) {
			collectionListenSupport.dispose();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.observable.list.ObservableList#dispose()
	 */
	public void dispose() {
		super.dispose();
		lastListenerRemoved();
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

