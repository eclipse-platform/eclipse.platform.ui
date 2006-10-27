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

package org.eclipse.jface.databinding.observable.set;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.databinding.observable.Diffs;
import org.eclipse.jface.databinding.observable.Realm;

/**
 * @since 1.0
 * 
 */
public class WritableSet extends ObservableSet {

	/**
	 * @param realm 
	 * @param wrappedSet
	 */
	public WritableSet() {
		this(Realm.getDefault(), Object.class);
	}

	/**
	 * @param realm 
	 * @param c
	 */
	public WritableSet(Collection c) {
		this(Realm.getDefault(), c, Object.class);
	}

	/**
	 * @param realm 
	 * @param c
	 * @param elementType 
	 */
	public WritableSet(Collection c, Object elementType) {
		this(Realm.getDefault(), new HashSet(c), elementType);
	}

	
	/**
	 * @param realm 
	 * @param elementType
	 */
	public WritableSet(Object elementType) {
		this(Realm.getDefault(), new HashSet(), elementType);
	}

	/**
	 * @param realm 
	 * @param wrappedSet
	 */
	public WritableSet(Realm realm) {
		this(realm, Object.class);
	}
	
	/**
	 * @param realm 
	 * @param c
	 */
	public WritableSet(Realm realm, Collection c) {
		this(realm, c, Object.class);
	}
	
	/**
	 * @param realm 
	 * @param c
	 * @param elementType 
	 */
	public WritableSet(Realm realm, Collection c, Object elementType) {
		super(realm, new HashSet(c), elementType);
		this.elementType = elementType;
	}
	
	/**
	 * @param realm 
	 * @param elementType
	 */
	public WritableSet(Realm realm, Object elementType) {
		super(realm, new HashSet(), elementType);
	}

	public boolean add(Object o) {
		boolean added = wrappedSet.add(o);
		if (added) {
			fireSetChange(Diffs.createSetDiff(Collections.singleton(o), Collections.EMPTY_SET));
		}
		return added;
	}

	public boolean addAll(Collection c) {
		Set adds = new HashSet();
		Iterator it = c.iterator();
		while (it.hasNext()) {
			Object element = it.next();
			if (wrappedSet.add(element)) {
				adds.add(element);
			}
		}
		if (adds.size() > 0) {
			fireSetChange(Diffs.createSetDiff(adds, Collections.EMPTY_SET));
			return true;
		}
		return false;
	}

	public boolean remove(Object o) {
		boolean removed = wrappedSet.remove(o);
		if (removed) {
			fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET, Collections
					.singleton(o)));
		}
		return removed;
	}

	public boolean removeAll(Collection c) {
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
		Set removes = new HashSet(wrappedSet);
		wrappedSet.clear();
		fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET, removes));
	}

}
