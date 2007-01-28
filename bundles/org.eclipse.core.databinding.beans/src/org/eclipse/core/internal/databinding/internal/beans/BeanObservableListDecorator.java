/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding.internal.beans;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;

/**
 * {@link IBeanObservable} decorator for an {@link IObservableList}.
 * 
 * @since 3.3
 */
public class BeanObservableListDecorator implements IObservableList,
		IBeanObservable {
	private IObservableList delegate;
	private Object observed;
	private PropertyDescriptor propertyDescriptor;

	/**
	 * @param delegate
	 * @param observed
	 * @param propertyDescriptor
	 */
	public BeanObservableListDecorator(IObservableList delegate,
			Object observed, PropertyDescriptor propertyDescriptor) {

		this.delegate = delegate;
		this.observed = observed;
		this.propertyDescriptor = propertyDescriptor;
	}

	public void add(int index, Object element) {
		delegate.add(index, element);
	}

	public boolean add(Object o) {
		return delegate.add(o);
	}

	public boolean addAll(Collection c) {
		return delegate.addAll(c);
	}

	public boolean addAll(int index, Collection c) {
		return delegate.addAll(index, c);
	}

	public void addChangeListener(IChangeListener listener) {
		delegate.addChangeListener(listener);
	}

	public void addListChangeListener(IListChangeListener listener) {
		delegate.addListChangeListener(listener);
	}

	public void addStaleListener(IStaleListener listener) {
		delegate.addStaleListener(listener);
	}

	public void clear() {
		delegate.clear();
	}

	public boolean contains(Object o) {
		return delegate.contains(o);
	}

	public boolean containsAll(Collection c) {
		return delegate.containsAll(c);
	}

	public void dispose() {
		delegate.dispose();
	}

	public boolean equals(Object o) {
		return delegate.equals(o);
	}

	public Object get(int index) {
		return delegate.get(index);
	}

	public Object getElementType() {
		return delegate.getElementType();
	}

	public Realm getRealm() {
		return delegate.getRealm();
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public int indexOf(Object o) {
		return delegate.indexOf(o);
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public boolean isStale() {
		return delegate.isStale();
	}

	public Iterator iterator() {
		return delegate.iterator();
	}

	public int lastIndexOf(Object o) {
		return delegate.lastIndexOf(o);
	}

	public ListIterator listIterator() {
		return delegate.listIterator();
	}

	public ListIterator listIterator(int index) {
		return delegate.listIterator(index);
	}

	public Object remove(int index) {
		return delegate.remove(index);
	}

	public boolean remove(Object o) {
		return delegate.remove(o);
	}

	public boolean removeAll(Collection c) {
		return delegate.removeAll(c);
	}

	public void removeChangeListener(IChangeListener listener) {
		delegate.removeChangeListener(listener);
	}

	public void removeListChangeListener(IListChangeListener listener) {
		delegate.removeListChangeListener(listener);
	}

	public void removeStaleListener(IStaleListener listener) {
		delegate.removeStaleListener(listener);
	}

	public boolean retainAll(Collection c) {
		return delegate.retainAll(c);
	}

	public Object set(int index, Object element) {
		return delegate.set(index, element);
	}

	public int size() {
		return delegate.size();
	}

	public List subList(int fromIndex, int toIndex) {
		return delegate.subList(fromIndex, toIndex);
	}

	public Object[] toArray() {
		return delegate.toArray();
	}

	public Object[] toArray(Object[] a) {
		return delegate.toArray(a);
	}

	/**
	 * @return list being delegated to
	 */
	public IObservableList getDelegate() {
		return delegate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.databinding.beans.IBeanObservable#getObserved()
	 */
	public Object getObserved() {
		return observed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.databinding.beans.IBeanObservable#getPropertyDescriptor()
	 */
	public PropertyDescriptor getPropertyDescriptor() {
		return propertyDescriptor;
	}
}
