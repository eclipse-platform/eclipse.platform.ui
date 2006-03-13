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
package org.eclipse.jface.internal.databinding.provisional.viewers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.internal.databinding.provisional.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.observable.IStaleListener;
import org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet;
import org.eclipse.jface.internal.databinding.provisional.observable.set.ISetChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.set.ObservableSet;
import org.eclipse.jface.internal.databinding.provisional.observable.set.SetDiff;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 1.0
 * 
 */
public final class ObservableSetContentProvider implements
		IStructuredContentProvider {

	private class KnownElementsSet extends ObservableSet {

		KnownElementsSet(Set wrappedSet) {
			super(wrappedSet, Object.class);
		}

		void doFireDiff(Set added, Set removed) {
			fireSetChange(Diffs.createSetDiff(added, removed));
		}

		void doFireStale(boolean isStale) {
			if (isStale) {
				fireStale();
			} else {
				fireChange();
			}
		}
	}

	private IObservableSet readableSet = new ObservableSet(
			Collections.EMPTY_SET, Object.class) {
	};

	private Viewer viewer;

	/**
	 * This readableSet returns the same elements as the input readableSet.
	 * However, it only fires events AFTER the elements have been added or
	 * removed from the viewer.
	 */
	private KnownElementsSet knownElements = new KnownElementsSet(readableSet);

	private ISetChangeListener listener = new ISetChangeListener() {

		public void handleSetChange(IObservableSet source, SetDiff diff) {
			boolean wasStale = knownElements.isStale();
			if (isDisposed()) {
				return;
			}
			doDiff(diff.getAdditions(), diff.getRemovals(), true);
			if (!wasStale && source.isStale()) {
				knownElements.doFireStale(true);
			}
		}
	};

	private IStaleListener staleListener = new IStaleListener() {
		public void handleStale(IObservable source) {
			knownElements.doFireStale(source.isStale());
		}
	};

	/**
	 * 
	 */
	public ObservableSetContentProvider() {
	}

	public void dispose() {
		setInput(null);
	}

	private void doDiff(Set added, Set removed, boolean updateViewer) {
		knownElements.doFireDiff(added, Collections.EMPTY_SET);

		if (updateViewer) {
			Object[] toAdd = added.toArray();
			if (viewer instanceof TableViewer) {
				TableViewer tv = (TableViewer) viewer;
				tv.add(toAdd);
			} else if (viewer instanceof AbstractListViewer) {
				AbstractListViewer lv = (AbstractListViewer) viewer;
				lv.add(toAdd);
			}
			Object[] toRemove = removed.toArray();
			if (viewer instanceof TableViewer) {
				TableViewer tv = (TableViewer) viewer;
				tv.remove(toRemove);
			} else if (viewer instanceof AbstractListViewer) {
				AbstractListViewer lv = (AbstractListViewer) viewer;
				lv.remove(toRemove);
			}
		}
		knownElements.doFireDiff(Collections.EMPTY_SET, removed);
	}

	public Object[] getElements(Object inputElement) {
		return readableSet.toArray();
	}

	/**
	 * Returns the readableSet of elements known to this content provider. Items
	 * are added to this readableSet before being added to the viewer, and they
	 * are removed after being removed from the viewer. The readableSet is
	 * always updated after the viewer. This is intended for use by label
	 * providers, as it will always return the items that need labels.
	 * 
	 * @return readableSet of items that will need labels
	 */
	public IObservableSet getKnownElements() {
		return knownElements;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;

		if (!(viewer instanceof TableViewer || viewer instanceof AbstractListViewer)) {
			throw new IllegalArgumentException(
					"This content provider only works with TableViewer or AbstractListViewer"); //$NON-NLS-1$
		}

		if (newInput != null && !(newInput instanceof IObservableSet)) {
			throw new IllegalArgumentException(
					"This content provider only works with input of type IReadableSet"); //$NON-NLS-1$
		}

		setInput((IObservableSet) newInput);
	}

	private boolean isDisposed() {
		return viewer.getControl() == null || viewer.getControl().isDisposed();
	}

	private void setInput(IObservableSet newSet) {
		boolean updateViewer = true;
		if (newSet == null) {
			newSet = new ObservableSet(Collections.EMPTY_SET, Object.class) {
			};
			// don't update the viewer - its input is null
			updateViewer = false;
		}

		boolean wasStale = false;
		if (readableSet != null) {
			wasStale = readableSet.isStale();
			readableSet.removeSetChangeListener(listener);
			readableSet.removeStaleListener(staleListener);
		}

		HashSet additions = new HashSet();
		HashSet removals = new HashSet();

		additions.addAll(newSet);
		additions.removeAll(readableSet);

		removals.addAll(readableSet);
		removals.removeAll(newSet);

		readableSet = newSet;

		doDiff(additions, removals, updateViewer);

		if (readableSet != null) {
			readableSet.addSetChangeListener(listener);
			readableSet.addStaleListener(staleListener);
		}

		boolean isStale = (readableSet != null && readableSet.isStale());
		if (isStale != wasStale) {
			knownElements.doFireStale(isStale);
		}
	}

}
