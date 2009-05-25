/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 208332, 265727
 *******************************************************************************/

package org.eclipse.core.databinding.observable.set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.internal.databinding.observable.IStalenessConsumer;
import org.eclipse.core.internal.databinding.observable.StalenessTracker;

/**
 * Represents a set consisting of the union of elements from one or more other
 * sets. This object does not need to be explicitly disposed. If nobody is
 * listening to the UnionSet, the set will remove its listeners.
 * 
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 * 
 * @since 1.0
 */
public final class UnionSet extends ObservableSet {

	/**
	 * child sets
	 */
	private IObservableSet[] childSets;

	private boolean stale = false;

	/**
	 * Map of elements onto Integer reference counts. This map is constructed
	 * when the first listener is added to the union set. Null if nobody is
	 * listening to the UnionSet.
	 */
	private HashMap refCounts = null;

	private StalenessTracker stalenessTracker;

	/**
	 * @param childSets
	 */
	public UnionSet(IObservableSet[] childSets) {
		this(childSets, childSets[0].getElementType());
	}

	/**
	 * @param childSets
	 * @param elementType
	 * @since 1.2
	 */
	public UnionSet(IObservableSet[] childSets, Object elementType) {
		super(childSets[0].getRealm(), null, elementType);
		System.arraycopy(childSets, 0,
				this.childSets = new IObservableSet[childSets.length], 0,
				childSets.length);
		this.stalenessTracker = new StalenessTracker(childSets,
				stalenessConsumer);
	}

	private ISetChangeListener childSetChangeListener = new ISetChangeListener() {
		public void handleSetChange(SetChangeEvent event) {
			processAddsAndRemoves(event.diff.getAdditions(), event.diff
					.getRemovals());
		}
	};

	private IStalenessConsumer stalenessConsumer = new IStalenessConsumer() {
		public void setStale(boolean stale) {
			boolean oldStale = UnionSet.this.stale;
			UnionSet.this.stale = stale;
			if (stale && !oldStale) {
				fireStale();
			}
		}
	};

	public boolean isStale() {
		getterCalled();
		if (refCounts != null) {
			return stale;
		}

		for (int i = 0; i < childSets.length; i++) {
			IObservableSet childSet = childSets[i];

			if (childSet.isStale()) {
				return true;
			}
		}
		return false;
	}

	private void processAddsAndRemoves(Set adds, Set removes) {
		Set addsToFire = new HashSet();
		Set removesToFire = new HashSet();

		for (Iterator iter = adds.iterator(); iter.hasNext();) {
			Object added = iter.next();

			Integer refCount = (Integer) refCounts.get(added);
			if (refCount == null) {
				refCounts.put(added, new Integer(1));
				addsToFire.add(added);
			} else {
				int refs = refCount.intValue();
				refCount = new Integer(refs + 1);
				refCounts.put(added, refCount);
			}
		}

		for (Iterator iter = removes.iterator(); iter.hasNext();) {
			Object removed = iter.next();

			Integer refCount = (Integer) refCounts.get(removed);
			if (refCount != null) {
				int refs = refCount.intValue();
				if (refs <= 1) {
					removesToFire.add(removed);
					refCounts.remove(removed);
				} else {
					refCount = new Integer(refCount.intValue() - 1);
					refCounts.put(removed, refCount);
				}
			}
		}

		// just in case the removes overlapped with the adds
		addsToFire.removeAll(removesToFire);

		if (addsToFire.size() > 0 || removesToFire.size() > 0) {
			fireSetChange(Diffs.createSetDiff(addsToFire, removesToFire));
		}
	}

	protected void firstListenerAdded() {
		super.firstListenerAdded();

		refCounts = new HashMap();
		for (int i = 0; i < childSets.length; i++) {
			IObservableSet next = childSets[i];
			next.addSetChangeListener(childSetChangeListener);
			incrementRefCounts(next);
		}
		stalenessTracker = new StalenessTracker(childSets, stalenessConsumer);
		setWrappedSet(refCounts.keySet());
	}

	protected void lastListenerRemoved() {
		super.lastListenerRemoved();

		for (int i = 0; i < childSets.length; i++) {
			IObservableSet next = childSets[i];

			next.removeSetChangeListener(childSetChangeListener);
			stalenessTracker.removeObservable(next);
		}
		refCounts = null;
		stalenessTracker = null;
		setWrappedSet(null);
	}

	private ArrayList incrementRefCounts(Collection added) {
		ArrayList adds = new ArrayList();

		for (Iterator iter = added.iterator(); iter.hasNext();) {
			Object next = iter.next();

			Integer refCount = (Integer) refCounts.get(next);
			if (refCount == null) {
				adds.add(next);
				refCount = new Integer(1);
				refCounts.put(next, refCount);
			} else {
				refCount = new Integer(refCount.intValue() + 1);
				refCounts.put(next, refCount);
			}
		}
		return adds;
	}

	protected void getterCalled() {
		super.getterCalled();
		if (refCounts == null) {
			// no listeners, recompute
			setWrappedSet(computeElements());
		}
	}

	private Set computeElements() {
		// If there is no cached value, compute the union from scratch
		if (refCounts == null) {
			Set result = new HashSet();
			for (int i = 0; i < childSets.length; i++) {
				result.addAll(childSets[i]);
			}
			return result;
		}

		// Else there is a cached value. Return it.
		return refCounts.keySet();
	}

}
