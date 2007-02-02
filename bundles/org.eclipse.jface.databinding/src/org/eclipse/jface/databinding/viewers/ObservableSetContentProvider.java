/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 ******************************************************************************/
package org.eclipse.jface.databinding.viewers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

/**
 * @since 1.0
 * 
 */
public final class ObservableSetContentProvider implements
		IStructuredContentProvider {

	private class KnownElementsSet extends ObservableSet {

		KnownElementsSet(Set wrappedSet) {
			super(SWTObservables.getRealm(Display.getDefault()), wrappedSet, Object.class);
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

	private IObservableSet readableSet;

	private Viewer viewer;

	/**
	 * This readableSet returns the same elements as the input readableSet.
	 * However, it only fires events AFTER the elements have been added or
	 * removed from the viewer.
	 */
	private KnownElementsSet knownElements;

	private ISetChangeListener listener = new ISetChangeListener() {

		public void handleSetChange(SetChangeEvent event) {
			boolean wasStale = knownElements.isStale();
			if (isDisposed()) {
				return;
			}
			doDiff(event.diff.getAdditions(), event.diff.getRemovals(), true);
			if (!wasStale && event.getObservableSet().isStale()) {
				knownElements.doFireStale(true);
			}
		}
	};

	private IStaleListener staleListener = new IStaleListener() {
		public void handleStale(StaleEvent event) {
			knownElements.doFireStale(event.getObservable().isStale());
		}
	};

	/**
	 * 
	 */
	public ObservableSetContentProvider() {
		readableSet = new ObservableSet(SWTObservables.getRealm(Display.getDefault()),
				Collections.EMPTY_SET, Object.class) {
		};        
		knownElements = new KnownElementsSet(readableSet);
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
			newSet = new ObservableSet(SWTObservables.getRealm(Display.getDefault()), Collections.EMPTY_SET, Object.class) {
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
