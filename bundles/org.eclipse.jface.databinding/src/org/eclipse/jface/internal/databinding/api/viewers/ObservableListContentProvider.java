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

package org.eclipse.jface.internal.databinding.api.viewers;

import org.eclipse.jface.internal.databinding.api.observable.list.IListChangeListener;
import org.eclipse.jface.internal.databinding.api.observable.list.IListDiff;
import org.eclipse.jface.internal.databinding.api.observable.list.IListDiffEntry;
import org.eclipse.jface.internal.databinding.api.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.api.observable.list.WritableList;
import org.eclipse.jface.internal.databinding.api.observable.set.IObservableSet;
import org.eclipse.jface.internal.databinding.api.observable.set.WritableSet;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.2
 * 
 */
public class ObservableListContentProvider implements
		IStructuredContentProvider {

	private IObservableList observableList = new WritableList();

	private Viewer viewer;

	private IListChangeListener listener = new IListChangeListener() {
		public void handleListChange(IObservableList source, IListDiff diff) {
			if (viewer.getControl().isDisposed()) {
				return;
			}
			IListDiffEntry[] differences = diff.getDifferences();
			for (int i = 0; i < differences.length; i++) {
				IListDiffEntry entry = differences[i];
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
	 * @return
	 */
	public IObservableSet getKnownElements() {
		return knownElements;
	}

}
