/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds (bug 136532)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.internal.viewers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.databinding.observable.Diffs;
import org.eclipse.jface.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.observable.list.ListDiffEntry;
import org.eclipse.jface.databinding.observable.list.ObservableList;
import org.eclipse.jface.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.observable.set.ISetChangeListener;
import org.eclipse.jface.databinding.observable.set.SetDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.IMultiMapping;
import org.eclipse.jface.internal.databinding.provisional.viewers.IObservableCollectionWithLabels;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.2
 * 
 */
public abstract class StructuredViewerObservableCollectionWithLabels extends
		ObservableList implements IObservableList, IObservableSet,
		IObservableCollectionWithLabels {

	private StructuredViewer structuredViewer;

	private ContentProvider contentProvider = new ContentProvider();

	private Object setChangeListeners;

	private Set elementsAsSet = new HashSet();

	private class ContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return StructuredViewerObservableCollectionWithLabels.this
					.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	/**
	 * @param structuredViewer
	 */
	public StructuredViewerObservableCollectionWithLabels(
			StructuredViewer structuredViewer) {
		super(new ArrayList(), Object.class);
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

	public void addSetChangeListener(ISetChangeListener listener) {
		if (setChangeListeners == null) {
			boolean hadListeners = hasListeners();
			setChangeListeners = listener;
			if (!hadListeners) {
				firstListenerAdded();
			}
			return;
		}

		Collection listenerList;
		if (setChangeListeners instanceof Collection) {
			listenerList = (Collection) setChangeListeners;
		} else {
			ISetChangeListener l = (ISetChangeListener) setChangeListeners;

			listenerList = new ArrayList();
			listenerList.add(l);
			setChangeListeners = listenerList;
		}

		listenerList.add(listener);
	}

	public void removeSetChangeListener(ISetChangeListener listener) {

		if (setChangeListeners == listener) {
			setChangeListeners = null;
			if (!hasListeners()) {
				lastListenerRemoved();
			}
			return;
		}

		if (setChangeListeners instanceof Collection) {
			Collection listenerList = (Collection) setChangeListeners;
			listenerList.remove(listener);
			if (listenerList.isEmpty()) {
				setChangeListeners = null;
				if (!hasListeners()) {
					lastListenerRemoved();
				}
			}
		}
	}

	protected boolean hasListeners() {
		return super.hasListeners() || setChangeListeners != null;
	}

	protected void fireSetChange(SetDiff diff) {
		if (setChangeListeners == null) {
			return;
		}

		if (setChangeListeners instanceof ISetChangeListener) {
			((ISetChangeListener) setChangeListeners).handleSetChange(this,
					diff);
			return;
		}

		Collection changeListenerCollection = (Collection) setChangeListeners;

		ISetChangeListener[] listeners = (ISetChangeListener[]) (changeListenerCollection)
				.toArray(new ISetChangeListener[changeListenerCollection.size()]);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].handleSetChange(this, diff);
		}
	}

	public boolean add(Object o) {
		boolean added = elementsAsSet.add(o);
		if (added) {
			wrappedList.add(o);
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(
					wrappedList.size() - 1, true, o)));
			fireSetChange(Diffs.createSetDiff(Collections.singleton(o),
					Collections.EMPTY_SET));
			// add to table after firing
			addToViewer(o);
		}
		return added;
	}

	/**
	 * Adds items contained in <code>c</code> that didn't previously exist in
	 * the viewer. This check is based on object equality. If new entries are to
	 * be added list change events and set change events are fired before the
	 * items are added to the viewer.
	 * 
	 * @param c items to be added
	 * @return success <code>true</code> if items were added to the list
	 * @see org.eclipse.jface.databinding.observable.list.IListChangeListener
	 * @see org.eclipse.jface.databinding.observable.set.ISetChangeListener
	 */
	public boolean addAll(Collection c) {
		if (c == null)
			throw new IllegalArgumentException("The 'c' parameter is null."); //$NON-NLS-1$
		
		// List of items being added to the viewer.
		List adds = new LinkedList();
		List listAdds = new ArrayList();
		Iterator it = c.iterator();
		while (it.hasNext()) {
			Object element = it.next();
			if (elementsAsSet.add(element)) {
				listAdds.add(Diffs.createListDiffEntry(wrappedList.size(),
						true, element));
				wrappedList.add(element);
				adds.add(element);
			}
		}
		if (adds.size() > 0) {
			fireListChange(Diffs.createListDiff((ListDiffEntry[]) listAdds
					.toArray(new ListDiffEntry[listAdds.size()])));
			fireSetChange(Diffs.createSetDiff(new HashSet(adds),
					Collections.EMPTY_SET));
			// add to viewer after firing
			addToViewer(adds.toArray());
			return true;
		}
		return false;
	}

	public boolean remove(Object o) {
		boolean removed = elementsAsSet.remove(o);
		if (removed) {
			int indexOfElement = wrappedList.indexOf(o);
			wrappedList.remove(indexOfElement);
			// remove from viewer before firing
			removeFromViewer(o);
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(
					indexOfElement, false, o)));
			fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET,
					Collections.singleton(o)));
		}
		return removed;
	}

	public boolean removeAll(Collection c) {
		Set removes = new HashSet();
		List listRemoves = new ArrayList();
		Iterator it = c.iterator();
		while (it.hasNext()) {
			Object element = it.next();
			if (elementsAsSet.remove(element)) {
				int indexOfElement = wrappedList.indexOf(element);
				wrappedList.remove(indexOfElement);
				listRemoves.add(Diffs.createListDiffEntry(indexOfElement,
						false, element));
				removes.add(element);
			}
		}
		if (removes.size() > 0) {
			// remove from viewer before firing
			removeFromViewer(removes.toArray());
			fireListChange(Diffs.createListDiff((ListDiffEntry[]) listRemoves
					.toArray(new ListDiffEntry[listRemoves.size()])));
			fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET, removes));
			return true;
		}
		return false;
	}

	public boolean retainAll(Collection c) {
		Set removes = new HashSet();
		List listRemoves = new ArrayList();
		Iterator it = wrappedList.iterator();
		for (int index = 0; it.hasNext(); index++) {
			Object element = it.next();
			if (!c.contains(element)) {
				it.remove();
				elementsAsSet.remove(element);
				removes.add(element);
				listRemoves.add(Diffs
						.createListDiffEntry(index, false, element));
			}
		}
		if (removes.size() > 0) {
			// remove from viewer before firing
			removeFromViewer(removes.toArray());
			fireListChange(Diffs.createListDiff((ListDiffEntry[]) listRemoves
					.toArray(new ListDiffEntry[listRemoves.size()])));
			fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET, removes));
			return true;
		}
		return false;
	}

	public void clear() {
		Set removes = new HashSet(elementsAsSet);
		List listRemoves = new ArrayList();
		Iterator it = wrappedList.iterator();
		for (int index = 0; it.hasNext(); index++) {
			Object element = it.next();
			listRemoves.add(Diffs.createListDiffEntry(index, false, element));
		}
		wrappedList.clear();
		elementsAsSet.clear();
		// refresh before firing
		structuredViewer.refresh();
		fireListChange(Diffs.createListDiff((ListDiffEntry[]) listRemoves
				.toArray(new ListDiffEntry[listRemoves.size()])));
		fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET, removes));
	}

	public Object set(int index, Object element) {
		Object oldObject = wrappedList.get(index);
		elementsAsSet.remove(oldObject);
		removeFromViewer(oldObject);
		fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(index,
				false, oldObject)));
		fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET, Collections
				.singleton(oldObject)));
		if (elementsAsSet.add(element)) {
			wrappedList.add(index, element);
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(
					index, true, element)));
			fireSetChange(Diffs.createSetDiff(Collections.singleton(element),
					Collections.EMPTY_SET));
			addToViewer(index, element);
		}
		return oldObject;
	}

	public Object remove(int index) {
		if (index < 0 || index >= wrappedList.size()) {
			throw new IllegalArgumentException("Request to remove a nonexistant list element"); //$NON-NLS-1$
		}
		Object oldObject = null;
		oldObject = wrappedList.remove(index);
		elementsAsSet.remove(oldObject);
		removeFromViewer(oldObject);
		fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(
				index, false, oldObject)));
		fireSetChange(Diffs.createSetDiff(Collections.EMPTY_SET,
				Collections.singleton(oldObject)));
		return oldObject;
	}

	 public void add(int index, Object element) {
		if (elementsAsSet.add(element)) {
			wrappedList.add(index, element);
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(
					index, true, element)));
			fireSetChange(Diffs.createSetDiff(Collections.singleton(element),
					Collections.EMPTY_SET));
			addToViewer(index, element);
		}
	}

	public boolean addAll(int index, Collection c) {
		Set adds = new HashSet();
		List listAdds = new ArrayList();
		Iterator it = c.iterator();
		while (it.hasNext()) {
			Object element = it.next();
			if (elementsAsSet.add(element)) {
				listAdds.add(Diffs.createListDiffEntry(index, true, element));
				wrappedList.add(index, element);
				adds.add(element);
				addToViewer(index, element);
				index++;
			}
		}
		if (adds.size() > 0) {
			fireListChange(Diffs.createListDiff((ListDiffEntry[]) listAdds
					.toArray(new ListDiffEntry[listAdds.size()])));
			fireSetChange(Diffs.createSetDiff(adds, Collections.EMPTY_SET));
			// add to viewer after firing
			return true;
		}
		return false;
	}

	public void dispose() {
		super.dispose();
		wrappedList.clear();
		structuredViewer = null;
		contentProvider = null;
	}

	protected abstract void addToViewer(Object element);

	protected abstract void addToViewer(Object[] elements);

	protected abstract void addToViewer(int index, Object element);

	protected abstract void removeFromViewer(Object element);

	protected abstract void removeFromViewer(Object[] elements);

	/**
	 * @return Returns the structuredViewer.
	 */
	public StructuredViewer getViewer() {
		return structuredViewer;
	}

}
