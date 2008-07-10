/*******************************************************************************
 * Copyright (c) 2005, 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 239302)
 *******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.jface.databinding.viewers.IViewerObservableSet;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * An observable set that tracks the filters of the given viewer. Note that this
 * set will not track changes that are made using direct API on StructuredViewer
 * (by calling
 * {@link StructuredViewer#addFilter(org.eclipse.jface.viewers.ViewerFilter)
 * addFilter()},
 * {@link StructuredViewer#removeFilter(org.eclipse.jface.viewers.ViewerFilter)
 * removeFilter()}, or
 * {@link StructuredViewer#setFilters(org.eclipse.jface.viewers.ViewerFilter[])
 * setFilters()}) -- it is assumed that filters are only changed through the
 * set.
 * 
 * @since 1.2
 */
public class ViewerFiltersObservableSet extends ObservableSet implements
		IViewerObservableSet {

	private StructuredViewer viewer;

	/**
	 * @param realm
	 * @param viewer
	 */
	public ViewerFiltersObservableSet(Realm realm, StructuredViewer viewer) {
		super(realm, new HashSet(Arrays.asList(viewer.getFilters())),
				ViewerFilter.class);
		this.viewer = viewer;
	}

	public Viewer getViewer() {
		return viewer;
	}

	private void replaceFilters() {
		viewer.getControl().setRedraw(false);
		try {
			viewer.setFilters((ViewerFilter[]) wrappedSet
					.toArray(new ViewerFilter[wrappedSet.size()]));
		} finally {
			viewer.getControl().setRedraw(true);
		}
	}

	private void addFilter(ViewerFilter filter) {
		viewer.getControl().setRedraw(false);
		try {
			viewer.addFilter(filter);
		} finally {
			viewer.getControl().setRedraw(true);
		}
	}

	private void removeFilter(ViewerFilter filter) {
		viewer.getControl().setRedraw(false);
		try {
			viewer.removeFilter(filter);
		} finally {
			viewer.getControl().setRedraw(true);
		}
	}

	public boolean add(Object element) {
		checkRealm();
		boolean added = wrappedSet.add(element);
		if (added) {
			addFilter((ViewerFilter) element);
			fireSetChange(Diffs.createSetDiff(Collections.singleton(element),
					Collections.EMPTY_SET));
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
			replaceFilters();
			fireSetChange(Diffs.createSetDiff(additions, Collections.EMPTY_SET));
			return true;
		}
		return false;
	}

	public void clear() {
		getterCalled();
		Set removes = new HashSet(wrappedSet);
		wrappedSet.clear();
		replaceFilters();
		fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET, removes));
	}

	public boolean remove(Object o) {
		getterCalled();
		boolean removed = wrappedSet.remove(o);
		if (removed) {
			removeFilter((ViewerFilter) o);
			fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET,
					Collections.singleton(o)));
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
			replaceFilters();
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
			replaceFilters();
			fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET, removes));
			return true;
		}
		return false;
	}
}
