/*******************************************************************************
 * Copyright (c) 2006-2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 147515
 *     Matthew Hall - bug 221351
 *******************************************************************************/

package org.eclipse.core.databinding.observable.set;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;

/**
 * Mutable (writable) implementation of {@link IObservableSet}.
 * 
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 * 
 * @since 1.0
 */
public class WritableSet extends ObservableSet {

	/**
	 * Constructs a new empty instance in the default realm with a
	 * <code>null</code> element type.
	 * 
	 */
	public WritableSet() {
		this(Realm.getDefault());
	}

	/**
	 * Constructs a new instance in the default realm containing the
	 * elements of the given collection. Changes to the given collection after
	 * calling this method do not affect the contents of the created WritableSet.
	 * 
	 * @param c
	 * @param elementType
	 *            can be <code>null</code>
	 */
	public WritableSet(Collection c, Object elementType) {
		this(Realm.getDefault(), new HashSet(c), elementType);
	}

	/**
	 * Constructs a new empty instance in the given realm and a
	 * <code>null</code> element type.
	 * 
	 * @param realm
	 */
	public WritableSet(Realm realm) {
		this(realm, new HashSet(), null);
	}

	/**
	 * Constructs a new instance in the default realm with the given element
	 * type, containing the elements of the given collection. Changes to the
	 * given collection after calling this method do not affect the contents of
	 * the created WritableSet.
	 * 
	 * @param realm
	 * @param c
	 * @param elementType
	 *            can be <code>null</code>
	 */
	public WritableSet(Realm realm, Collection c, Object elementType) {
		super(realm, new HashSet(c), elementType);
		this.elementType = elementType;
	}

	public boolean add(Object o) {
		getterCalled();
		boolean added = wrappedSet.add(o);
		if (added) {
			fireSetChange(Diffs.createSetDiff(Collections.singleton(o), Collections.EMPTY_SET));
		}
		return added;
	}

	public boolean addAll(Collection c) {
		getterCalled();
		Set additions = new HashSet();
		Iterator it = c.iterator();
		while (it.hasNext()) {
			Object element = it.next();
			if (wrappedSet.add(element)) {
				additions.add(element);
			}
		}
		if (additions.size() > 0) {
			fireSetChange(Diffs.createSetDiff(additions, Collections.EMPTY_SET));
			return true;
		}
		return false;
	}

	public boolean remove(Object o) {
		getterCalled();
		boolean removed = wrappedSet.remove(o);
		if (removed) {
			fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET, Collections
					.singleton(o)));
		}
		return removed;
	}

	public boolean removeAll(Collection c) {
		getterCalled();
		Set removes = new HashSet();
		Iterator it = c.iterator();
		while (it.hasNext()) {
			Object element = it.next();
			if (wrappedSet.remove(element)) {
				removes.add(element);
			}
		}
		if (removes.size() > 0) {
			fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET, removes));
			return true;
		}
		return false;
	}

	public boolean retainAll(Collection c) {
		getterCalled();
		Set removes = new HashSet();
		Iterator it = wrappedSet.iterator();
		while (it.hasNext()) {
			Object element = it.next();
			if (!c.contains(element)) {
				it.remove();
				removes.add(element);
			}
		}
		if (removes.size() > 0) {
			fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET, removes));
			return true;
		}
		return false;
	}

	public void clear() {
		getterCalled();
		Set removes = new HashSet(wrappedSet);
		wrappedSet.clear();
		fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET, removes));
	}

	/**
	 * @param elementType can be <code>null</code>
	 * @return new instance with the default realm
	 */
	public static WritableSet withElementType(Object elementType) {
		return new WritableSet(Realm.getDefault(), new HashSet(), elementType);
	}
}
