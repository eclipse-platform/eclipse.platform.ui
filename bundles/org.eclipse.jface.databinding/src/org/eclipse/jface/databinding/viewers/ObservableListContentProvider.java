/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl<tom.schindl@bestsolution.at> - bugfix in: 214355
 *******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

/**
 * @since 1.1
 *
 */
public class ObservableListContentProvider implements
		IStructuredContentProvider {

	private IObservableList observableList;

	private Viewer viewer;

	private IListChangeListener listener = new IListChangeListener() {
		public void handleListChange(ListChangeEvent event) {
			if (viewer.getControl().isDisposed()) {
				return;
			}
			ListDiffEntry[] differences = event.diff.getDifferences();
			for (int i = 0; i < differences.length; i++) {
				ListDiffEntry entry = differences[i];
				if (entry.isAddition()) {
					knownElements.add(entry.getElement());
					if (viewer instanceof AbstractListViewer) {
						((AbstractListViewer) viewer).insert(entry.getElement(), entry.getPosition());
					} else {
						((AbstractTableViewer) viewer).insert(entry.getElement(), entry
								.getPosition());
					}
				} else {
					if (viewer instanceof AbstractListViewer) {
						((AbstractListViewer) viewer)
								.remove(entry.getElement());
					} else {
						((AbstractTableViewer) viewer).remove(entry.getElement());
					}
					knownElements.remove(entry.getElement());
				}
			}
		}
	};

	private IObservableSet knownElements;

	/**
	 *
	 */
	public ObservableListContentProvider() {
		observableList = new WritableList(SWTObservables.getRealm(Display.getDefault()));
		knownElements = new WritableSet(SWTObservables.getRealm(Display.getDefault()));
	}

	public Object[] getElements(Object inputElement) {
		return observableList.toArray();
	}

	public void dispose() {
		observableList.removeListChangeListener(listener);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;

		if (!(viewer instanceof AbstractTableViewer || viewer instanceof AbstractListViewer)) {
			throw new IllegalArgumentException(
					"This content provider only works with AbstractTableViewer or AbstractListViewer"); //$NON-NLS-1$
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
			list = new WritableList(SWTObservables.getRealm(Display.getDefault()));
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
