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

import org.eclipse.jface.internal.databinding.provisional.observable.list.IListChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.provisional.observable.list.ListDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.list.ListDiffEntry;
import org.eclipse.jface.internal.databinding.provisional.observable.list.WritableList;
import org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet;
import org.eclipse.jface.internal.databinding.provisional.observable.set.WritableSet;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 1.0
 * 
 */
public class ObservableListContentProvider implements
		IStructuredContentProvider {

	private IObservableList observableList = new WritableList();

	private Viewer viewer;

	private IListChangeListener listener = new IListChangeListener() {
		public void handleListChange(IObservableList source, ListDiff diff) {
			if (viewer.getControl().isDisposed()) {
				return;
			}
			ListDiffEntry[] differences = diff.getDifferences();
			for (int i = 0; i < differences.length; i++) {
				ListDiffEntry entry = differences[i];
				if (entry.isAddition()) {
					knownElements.add(entry.getElement());
					if (viewer instanceof AbstractListViewer) {
						((AbstractListViewer) viewer).add(entry.getElement());
					} else {
						((TableViewer) viewer).insert(entry.getElement(), entry
								.getPosition());
					}
				} else {
					if (viewer instanceof AbstractListViewer) {
						((AbstractListViewer) viewer)
								.remove(entry.getElement());
					} else {
						((TableViewer) viewer).remove(entry.getElement());
					}
					knownElements.remove(entry.getElement());
				}
			}
		}
	};

	private IObservableSet knownElements = new WritableSet();

	/**
	 * 
	 */
	public ObservableListContentProvider() {
	}

	public Object[] getElements(Object inputElement) {
		return observableList.toArray();
	}

	public void dispose() {
		observableList.removeListChangeListener(listener);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;

		if (!(viewer instanceof TableViewer || viewer instanceof AbstractListViewer)) {
			throw new IllegalArgumentException(
					"This content provider only works with TableViewer or AbstractListViewer"); //$NON-NLS-1$
		}

		if (newInput != null && !(newInput instanceof IObservableList)) {
			throw new IllegalArgumentException(
					"This content provider only works with input of type IObservableList"); //$NON-NLS-1$
		}

		setInput((IObservableList) newInput);
	}

	/**
	 * @param list
	 */
	private void setInput(IObservableList list) {

		if (list == null) {
			list = new WritableList();
		}

		if (observableList != null) {
			observableList.removeListChangeListener(listener);
		}
		
		knownElements.clear();

		observableList = list;
		
		knownElements.addAll(list);

		observableList.addListChangeListener(listener);
	}

	/**
	 * @return the set of elements known to this content provider. Label providers may track
	 * this set if they need to be notified about additions before the viewer sees the added
	 * element, and notified about removals after the element was removed from the viewer.
	 */
	public IObservableSet getKnownElements() {
		return knownElements;
	}

}
