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
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.BindingException;
import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.list.ObservableList;

/**
 * @since 1.0
 * 
 */
public class JavaBeanObservableList extends ObservableList implements
		IBeanObservable {

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
	public JavaBeanObservableList(Realm realm, Object object,
			PropertyDescriptor descriptor, Class elementType) {
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

	public void dispose() {
		super.dispose();
		lastListenerRemoved();
	}

	private Object primGetValues() {
		Exception ex = null;
		try {
			Method readMethod = descriptor.getReadMethod();
			if (!readMethod.isAccessible()) {
				readMethod.setAccessible(true);
			}
			return readMethod.invoke(object, new Object[0]);
		} catch (IllegalArgumentException e) {
			ex = e;
		} catch (IllegalAccessException e) {
			ex = e;
		} catch (InvocationTargetException e) {
			ex = e;
		}
		throw new BindingException("Could not read collection values", ex); //$NON-NLS-1$
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

	public Object getObserved() {
		return object;
	}

	public PropertyDescriptor getPropertyDescriptor() {
		return descriptor;
	}

	private void setValues() {
		if (descriptor.getPropertyType().isArray()) {
			Class componentType = descriptor.getPropertyType()
					.getComponentType();
			Object[] newArray = (Object[]) Array.newInstance(componentType,
					wrappedList.size());
			wrappedList.toArray(newArray);
			primSetValues(newArray);
		} else {
			// assume that it is a java.util.List
			primSetValues(new ArrayList(wrappedList));
		}
	}

	private void primSetValues(Object newValue) {
		Exception ex = null;
		try {
			Method writeMethod = descriptor.getWriteMethod();
			if (!writeMethod.isAccessible()) {
				writeMethod.setAccessible(true);
			}
			writeMethod.invoke(object, new Object[] { newValue });
			return;
		} catch (IllegalArgumentException e) {
			ex = e;
		} catch (IllegalAccessException e) {
			ex = e;
		} catch (InvocationTargetException e) {
			ex = e;
		}
		throw new BindingException("Could not write collection values", ex); //$NON-NLS-1$
	}

	public Object set(int index, Object element) {
		getterCalled();
		updating = true;
		try {
			Object oldElement = wrappedList.set(index, element);
			setValues();
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(
					index, true, element), Diffs.createListDiffEntry(index + 1,
					false, oldElement)));
			return oldElement;
		} finally {
			updating = false;
		}
	}

	public Object remove(int index) {
		getterCalled();
		updating = true;
		try {
			Object oldElement = wrappedList.remove(index);
			setValues();
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(
					index, false, oldElement)));
			return oldElement;
		} finally {
			updating = false;
		}
	}

	public boolean add(Object element) {
		updating = true;
		try {
			int index = wrappedList.size();
			boolean result = wrappedList.add(element);
			setValues();
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(
					index, true, element)));
			return result;
		} finally {
			updating = false;
		}
	}

	public void add(int index, Object element) {
		updating = true;
		try {
			wrappedList.add(index, element);
			setValues();
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(
					index, true, element)));
		} finally {
			updating = false;
		}
	}

	public boolean addAll(Collection c) {
		if (c.isEmpty()) {
			return false;
		}
		updating = true;
		try {
			int index = wrappedList.size();
			boolean result = wrappedList.addAll(c);
			setValues();
			ListDiffEntry[] entries = new ListDiffEntry[c.size()];
			int i = 0;
			for (Iterator it = c.iterator(); it.hasNext();) {
				Object o = it.next();
				entries[i++] = Diffs.createListDiffEntry(index++, true, o);
			}
			fireListChange(Diffs.createListDiff(entries));
			return result;
		} finally {
			updating = false;
		}
	}

	public boolean addAll(int index, Collection c) {
		if (c.isEmpty()) {
			return false;
		}
		updating = true;
		try {
			boolean result = wrappedList.addAll(index, c);
			setValues();
			ListDiffEntry[] entries = new ListDiffEntry[c.size()];
			int i = 0;
			for (Iterator it = c.iterator(); it.hasNext();) {
				Object o = it.next();
				entries[i++] = Diffs.createListDiffEntry(index++, true, o);
			}
			fireListChange(Diffs.createListDiff(entries));
			return result;
		} finally {
			updating = false;
		}
	}

	public boolean remove(Object o) {
		getterCalled();
		int index = wrappedList.indexOf(o);
		if (index == -1) {
			return false;
		}
		updating = true;
		try {
			Object oldElement = wrappedList.remove(index);
			setValues();
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(
					index, false, oldElement)));
			return true;
		} finally {
			updating = false;
		}
	}

	public boolean removeAll(Collection c) {
		getterCalled();
		boolean changed = false;
		updating = true;
		try {
			List diffEntries = new ArrayList();
			for (Iterator it = c.iterator(); it.hasNext();) {
				Object o = it.next();
				int index = wrappedList.indexOf(o);
				if (index != -1) {
					changed = true;
					Object oldElement = wrappedList.remove(index);
					diffEntries.add(Diffs.createListDiffEntry(index, false,
							oldElement));
				}
			}
			setValues();
			fireListChange(Diffs.createListDiff((ListDiffEntry[]) diffEntries
					.toArray(new ListDiffEntry[diffEntries.size()])));
			return changed;
		} finally {
			updating = false;
		}
	}

	public boolean retainAll(Collection c) {
		getterCalled();
		boolean changed = false;
		updating = true;
		try {
			List diffEntries = new ArrayList();
			int index = 0;
			for (Iterator it = wrappedList.iterator(); it.hasNext();) {
				Object o = it.next();
				boolean retain = c.contains(o);
				if (retain) {
					index++;
				} else {
					changed = true;
					it.remove();
					diffEntries.add(Diffs.createListDiffEntry(index, false, o));
				}
			}
			setValues();
			fireListChange(Diffs.createListDiff((ListDiffEntry[]) diffEntries
					.toArray(new ListDiffEntry[diffEntries.size()])));
			return changed;
		} finally {
			updating = false;
		}
	}

	public void clear() {
		updating = true;
		try {
			List diffEntries = new ArrayList();
			for (Iterator it = wrappedList.iterator(); it.hasNext();) {
				Object o = it.next();
				diffEntries.add(Diffs.createListDiffEntry(0, false, o));
			}
			setValues();
			fireListChange(Diffs.createListDiff((ListDiffEntry[]) diffEntries
					.toArray(new ListDiffEntry[diffEntries.size()])));
		} finally {
			updating = false;
		}
	}

}
