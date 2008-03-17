/*******************************************************************************
 * Copyright (c) 2006-2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl<tom.schindl@bestsolution.at> - bugfix in: 214355
 *     Matthew Hall - bug 215531
 *******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;

/**
 * An {@link IStructuredContentProvider} for {@link AbstractTableViewer} or
 * {@link AbstractListViewer} that provides elements of an
 * {@link IObservableList} when set as the viewer's input. Changes in the
 * observed list (additions and removals) are reflected in the viewer.
 * 
 * @since 1.1
 */
public class ObservableListContentProvider extends
		ObservableCollectionContentProvider {
	/**
	 * Constructs an ObservableListContentProvider
	 */
	public ObservableListContentProvider() {
	}

	void checkInput(Object input) {
		Assert
				.isTrue(input instanceof IObservableList,
						"This content provider only works with input of type IObservableList"); //$NON-NLS-1$
	}

	private IListChangeListener changeListener = new IListChangeListener() {
		public void handleListChange(ListChangeEvent event) {
			if (isViewerDisposed())
				return;
			event.diff.accept(new ListDiffVisitor() {
				public void handleAdd(int index, Object element) {
					knownElements.add(element);
					viewerUpdater.insert(element, index);
				}

				public void handleRemove(int index, Object element) {
					viewerUpdater.remove(element, index);
					knownElements.remove(element);
				}

				public void handleReplace(int index, Object oldElement,
						Object newElement) {
					knownElements.add(newElement);
					viewerUpdater.replace(oldElement, newElement, index);
					knownElements.remove(oldElement);
				}

				public void handleMove(int oldIndex, int newIndex,
						Object element) {
					viewerUpdater.remove(element, oldIndex);
					viewerUpdater.insert(element, newIndex);
				}
			});
		}
	};

	void addCollectionChangeListener(IObservableCollection collection) {
		((IObservableList) collection).addListChangeListener(changeListener);
	}

	void removeCollectionChangeListener(IObservableCollection collection) {
		((IObservableList) collection).removeListChangeListener(changeListener);
	}
}
