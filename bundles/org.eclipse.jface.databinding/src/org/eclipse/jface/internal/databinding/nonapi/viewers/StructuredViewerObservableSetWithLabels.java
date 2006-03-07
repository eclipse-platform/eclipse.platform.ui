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

package org.eclipse.jface.internal.databinding.nonapi.viewers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.internal.databinding.api.observable.mapping.IMultiMapping;
import org.eclipse.jface.internal.databinding.api.observable.set.IObservableSetWithLabels;
import org.eclipse.jface.internal.databinding.api.observable.set.ObservableSet;
import org.eclipse.jface.internal.databinding.api.observable.set.SetDiff;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.2
 * 
 */
public abstract class StructuredViewerObservableSetWithLabels extends
		ObservableSet implements IObservableSetWithLabels {

	private StructuredViewer structuredViewer;

	private ContentProvider contentProvider = new ContentProvider();

	private class ContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return StructuredViewerObservableSetWithLabels.this.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	/**
	 * @param structuredViewer
	 */
	public StructuredViewerObservableSetWithLabels(
			StructuredViewer structuredViewer) {
		super(new HashSet(), Object.class);
		this.structuredViewer = structuredViewer;
		// set the content provider and input here and not in init().
		// This way, we can bind just the observable set (and not use the
		// labelMapping, i.e. a label provider has to be provided by the
		// client).
		structuredViewer.setContentProvider(contentProvider);
		structuredViewer.setInput(this);
	}

	public abstract void init(IMultiMapping labelMapping);

	public abstract void updateElements(Object[] elements);

	public boolean add(Object o) {
		boolean added = wrappedSet.add(o);
		if (added) {
			fireSetChange(new SetDiff(Collections.singleton(o),
					Collections.EMPTY_SET));
			// add to table after firing
			addToViewer(o);
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
			fireSetChange(new SetDiff(adds, Collections.EMPTY_SET));
			// add to viewer after firing
			addToViewer(adds.toArray());
			return true;
		}
		return false;
	}

	public boolean remove(Object o) {
		boolean removed = wrappedSet.remove(o);
		if (removed) {
			// remove from viewer before firing
			removeFromViewer(o);
			fireSetChange(new SetDiff(Collections.EMPTY_SET, Collections
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
			// remove from vieiwer before firing
			removeFromViewer(removes.toArray());
			fireSetChange(new SetDiff(Collections.EMPTY_SET, removes));
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
			// remove from viewer before firing
			removeFromViewer(removes.toArray());
			fireSetChange(new SetDiff(Collections.EMPTY_SET, removes));
			return true;
		}
		return false;
	}

	public void clear() {
		Set removes = new HashSet(wrappedSet);
		wrappedSet.clear();
		// refresh before firing
		structuredViewer.refresh();
		fireSetChange(new SetDiff(Collections.EMPTY_SET, removes));
	}

	public void dispose() {
		super.dispose();
		wrappedSet.clear();
		structuredViewer = null;
		contentProvider = null;
	}

	protected abstract void addToViewer(Object element);

	protected abstract void addToViewer(Object[] elements);

	protected abstract void removeFromViewer(Object element);

	protected abstract void removeFromViewer(Object[] elements);

	/**
	 * @return Returns the structuredViewer.
	 */
	public StructuredViewer getViewer() {
		return structuredViewer;
	}

}
