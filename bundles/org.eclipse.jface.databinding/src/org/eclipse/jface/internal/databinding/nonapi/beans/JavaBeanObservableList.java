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

package org.eclipse.jface.internal.databinding.nonapi.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.internal.databinding.api.observable.list.AbstractObservableList;
import org.eclipse.jface.internal.databinding.api.observable.list.IListDiffEntry;
import org.eclipse.jface.internal.databinding.api.observable.list.ListDiff;
import org.eclipse.jface.internal.databinding.api.observable.list.ListDiffEntry;
import org.eclipse.jface.util.Assert;

/**
 * @since 3.2
 * 
 */
public class JavaBeanObservableList extends AbstractObservableList {

	private final Object object;

	private PropertyChangeListener collectionListener = new PropertyChangeListener() {
		public void propertyChange(java.beans.PropertyChangeEvent event) {
			if (!updating) {
				if (JavaBeanObservableList.this.descriptor.getName().equals(
						event.getPropertyName())) {
					List diffEntries = new ArrayList();
					List newList = Arrays.asList(getValues());
					// TODO this is a naive list diff algorithm, we need a
					// smarter one
					for (Iterator it = wrappedList.iterator(); it.hasNext();) {
						Object oldElement = it.next();
						diffEntries
								.add(new ListDiffEntry(0, false, oldElement));
					}
					int i = 0;
					for (Iterator it = newList.iterator(); it.hasNext();) {
						Object newElement = it.next();
						diffEntries
								.add(new ListDiffEntry(i++, true, newElement));
					}
					wrappedList = newList;
					fireListChange(new ListDiff((IListDiffEntry[]) diffEntries
							.toArray(new IListDiffEntry[diffEntries.size()])));
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
	 * @param elementType
	 */
	public JavaBeanObservableList(Object object, PropertyDescriptor descriptor,
			Class elementType) {
		super(new ArrayList());
		this.object = object;
		this.descriptor = descriptor;
		this.elementType = elementType;
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

	/**
	 * @return the elementType
	 */
	public Class getElementType() {
		return elementType;
	}

}
