/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *     Brad Reynolds - bug 167204
 *     Gautam Saggar - bug 169529
 *     Brad Reynolds - bug 147515
 *******************************************************************************/
package org.eclipse.core.databinding.observable.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;

/**
 * Mutable observable list backed by an ArrayList.
 * 
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 * 
 * @since 1.0
 */
public class WritableList extends ObservableList {

	/**
	 * Creates an empty writable list in the default realm with a
	 * <code>null</code> element type.
	 * 
	 */
	public WritableList() {
		this(Realm.getDefault());
	}

	/**
	 * Creates an empty writable list with a <code>null</code> element type.
	 * 
	 * @param realm
	 */
	public WritableList(Realm realm) {
		this(realm, new ArrayList(), null);
	}

	/**
	 * Construts a new instance with the default realm.
	 * 
	 * @param toWrap
	 * @param elementType
	 *            can be <code>null</code>
	 */
	public WritableList(List toWrap, Object elementType) {
		this(Realm.getDefault(), toWrap, elementType);
	}

	/**
	 * Creates a writable list containing elements of the given type, wrapping
	 * an existing client-supplied list.
	 * 
	 * @param realm
	 * @param toWrap
	 *            The java.utilList to wrap
	 * @param elementType
	 *            can be <code>null</code>
	 */
	public WritableList(Realm realm, List toWrap, Object elementType) {
		super(realm, toWrap, elementType);
	}

	public Object set(int index, Object element) {
		checkRealm();
		Object oldElement = wrappedList.set(index, element);
		fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(index,
				false, oldElement), Diffs.createListDiffEntry(index, true,
				element)));
		return oldElement;
	}

	public Object remove(int index) {
		checkRealm();
		Object oldElement = wrappedList.remove(index);
		fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(index,
				false, oldElement)));
		return oldElement;
	}

	public boolean add(Object element) {
		checkRealm();
		boolean added = wrappedList.add(element);
		if (added) {
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(
					wrappedList.size() - 1, true, element)));
		}
		return added;
	}

	public void add(int index, Object element) {
		checkRealm();
		wrappedList.add(index, element);
		fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(index,
				true, element)));
	}

	public boolean addAll(Collection c) {
		checkRealm();
		ListDiffEntry[] entries = new ListDiffEntry[c.size()];
		int i = 0;
		int addIndex = wrappedList.size();
		for (Iterator it = c.iterator(); it.hasNext();) {
			Object element = it.next();
			entries[i++] = Diffs.createListDiffEntry(addIndex++, true, element);
		}
		boolean added = wrappedList.addAll(c);
		fireListChange(Diffs.createListDiff(entries));
		return added;
	}

	public boolean addAll(int index, Collection c) {
		checkRealm();
		ListDiffEntry[] entries = new ListDiffEntry[c.size()];
		int i = 0;
		int addIndex = index;
		for (Iterator it = c.iterator(); it.hasNext();) {
			Object element = it.next();
			entries[i++] = Diffs.createListDiffEntry(addIndex++, true, element);
		}
		boolean added = wrappedList.addAll(index, c);
		fireListChange(Diffs.createListDiff(entries));
		return added;
	}

	public boolean remove(Object o) {
		checkRealm();
		int index = wrappedList.indexOf(o);
		if (index == -1) {
			return false;
		}
		wrappedList.remove(index);
		fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(index,
				false, o)));
		return true;
	}

	public boolean removeAll(Collection c) {
		checkRealm();
		List entries = new ArrayList();
		for (Iterator it = c.iterator(); it.hasNext();) {
			Object element = it.next();
			int removeIndex = wrappedList.indexOf(element);
			if (removeIndex != -1) {
				wrappedList.remove(removeIndex);
				entries.add(Diffs.createListDiffEntry(removeIndex, false,
						element));
			}
		}
		fireListChange(Diffs.createListDiff((ListDiffEntry[]) entries
				.toArray(new ListDiffEntry[entries.size()])));
		return entries.size() > 0;
	}

	public boolean retainAll(Collection c) {
		checkRealm();
		List entries = new ArrayList();
		int removeIndex = 0;
		for (Iterator it = wrappedList.iterator(); it.hasNext();) {
			Object element = it.next();
			if (!c.contains(element)) {
				entries.add(Diffs.createListDiffEntry(removeIndex, false,
						element));
				it.remove();
			} else {
				// only increment if we haven't removed the current element
				removeIndex++;
			}
		}
		fireListChange(Diffs.createListDiff((ListDiffEntry[]) entries
				.toArray(new ListDiffEntry[entries.size()])));
		return entries.size() > 0;
	}

	public void clear() {
		checkRealm();
		List entries = new ArrayList();
		for (Iterator it = wrappedList.iterator(); it.hasNext();) {
			Object element = it.next();
			// always report 0 as the remove index
			entries.add(Diffs.createListDiffEntry(0, false, element));
			it.remove();
		}
		fireListChange(Diffs.createListDiff((ListDiffEntry[]) entries
				.toArray(new ListDiffEntry[entries.size()])));
	}

	/**
	 * @param elementType
	 *            can be <code>null</code>
	 * @return new list with the default realm.
	 */
	public static WritableList withElementType(Object elementType) {
		return new WritableList(Realm.getDefault(), new ArrayList(),
				elementType);
	}
}
