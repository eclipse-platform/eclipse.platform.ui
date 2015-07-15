/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 208332, 146397, 249526
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.runtime.Assert;

/**
 * Singleton empty set
 *
 * @param <E>
 *            the type of the object in this collection
 */
public class EmptyObservableSet<E> implements IObservableSet<E> {

	private final Set<E> emptySet = Collections.emptySet();

	private final Realm realm;
	private Object elementType;

	/**
	 * Creates a singleton empty set. This set may be disposed multiple times
	 * without any side-effects.
	 *
	 * @param realm
	 *            the realm of the constructed set
	 */
	public EmptyObservableSet(Realm realm) {
		this(realm, null);
	}

	/**
	 * Creates a singleton empty set. This set may be disposed multiple times
	 * without any side-effects.
	 *
	 * @param realm
	 *            the realm of the constructed set
	 * @param elementType
	 *            the element type of the constructed set
	 * @since 1.1
	 */
	public EmptyObservableSet(Realm realm, Object elementType) {
		this.realm = realm;
		this.elementType = elementType;
		ObservableTracker.observableCreated(this);
	}

	@Override
	public void addSetChangeListener(ISetChangeListener<? super E> listener) {
	}

	@Override
	public void removeSetChangeListener(ISetChangeListener<? super E> listener) {
	}

	@Override
	public Object getElementType() {
		return elementType;
	}

	@Override
	public int size() {
		checkRealm();
		return 0;
	}

	private void checkRealm() {
		Assert.isTrue(realm.isCurrent(),
				"Observable cannot be accessed outside its realm"); //$NON-NLS-1$
	}

	@Override
	public boolean isEmpty() {
		checkRealm();
		return true;
	}

	@Override
	public boolean contains(Object o) {
		checkRealm();
		return false;
	}

	@Override
	public Iterator<E> iterator() {
		checkRealm();
		return emptySet.iterator();
	}

	@Override
	public Object[] toArray() {
		checkRealm();
		return emptySet.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return emptySet.toArray(a);
	}

	@Override
	public boolean add(E o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		checkRealm();
		return c.isEmpty();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addChangeListener(IChangeListener listener) {
	}

	@Override
	public void removeChangeListener(IChangeListener listener) {
	}

	@Override
	public void addStaleListener(IStaleListener listener) {
	}

	@Override
	public void removeStaleListener(IStaleListener listener) {
	}

	@Override
	public void addDisposeListener(IDisposeListener listener) {
	}

	@Override
	public void removeDisposeListener(IDisposeListener listener) {
	}

	@Override
	public boolean isStale() {
		checkRealm();
		return false;
	}

	@Override
	public boolean isDisposed() {
		return false;
	}

	@Override
	public void dispose() {
	}

	@Override
	public Realm getRealm() {
		return realm;
	}

	@Override
	public boolean equals(Object obj) {
		checkRealm();
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Set))
			return false;

		return ((Set<?>) obj).isEmpty();
	}

	@Override
	public int hashCode() {
		checkRealm();
		return 0;
	}
}
