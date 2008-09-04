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
 *     Matthew Hall - bugs 215531, 226765, 222991
 *******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import java.util.Set;

import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.internal.databinding.viewers.ObservableCollectionContentProvider;
import org.eclipse.jface.internal.databinding.viewers.ViewerElementSet;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A {@link IStructuredContentProvider content provider} for
 * {@link AbstractTableViewer} or {@link AbstractListViewer} that provides
 * elements of an {@link IObservableList} when set as the viewer's input.
 * Objects of this class listen for changes to the observable list, and will
 * insert and remove viewer elements to reflect observed changes.
 * 
 * <p>
 * This class is not intended to be subclassed by clients.
 * 
 * @since 1.1
 */
public class ObservableListContentProvider implements
		IStructuredContentProvider {
	private ObservableCollectionContentProvider impl;

	private static class Impl extends ObservableCollectionContentProvider
			implements IListChangeListener {
		protected void checkInput(Object input) {
			Assert
					.isTrue(input instanceof IObservableList,
							"This content provider only works with input of type IObservableList"); //$NON-NLS-1$
		}

		protected void addCollectionChangeListener(
				IObservableCollection collection) {
			((IObservableList) collection).addListChangeListener(this);
		}

		protected void removeCollectionChangeListener(
				IObservableCollection collection) {
			((IObservableList) collection).removeListChangeListener(this);
		}

		public void handleListChange(ListChangeEvent event) {
			if (isViewerDisposed())
				return;

			// Determine which elements were added and removed
			final Set knownElementAdditions = ViewerElementSet
					.withComparer(comparer);
			final Set knownElementRemovals = ViewerElementSet
					.withComparer(comparer);
			event.diff.accept(new ListDiffVisitor() {
				public void handleAdd(int index, Object element) {
					knownElementAdditions.add(element);
				}

				public void handleRemove(int index, Object element) {
					knownElementRemovals.add(element);
				}
			});
			knownElementAdditions.removeAll(knownElements);
			knownElementRemovals.removeAll(event.getObservableList());

			knownElements.addAll(knownElementAdditions);
			if (realizedElements != null) {
				realizedElements.removeAll(knownElementRemovals);
			}

			event.diff.accept(new ListDiffVisitor() {
				public void handleAdd(int index, Object element) {
					viewerUpdater.insert(element, index);
				}

				public void handleRemove(int index, Object element) {
					viewerUpdater.remove(element, index);
				}

				public void handleReplace(int index, Object oldElement,
						Object newElement) {
					viewerUpdater.replace(oldElement, newElement, index);
				}

				public void handleMove(int oldIndex, int newIndex,
						Object element) {
					viewerUpdater.move(element, oldIndex, newIndex);
				}
			});

			if (realizedElements != null) {
				realizedElements.addAll(knownElementAdditions);
			}
			knownElements.removeAll(knownElementRemovals);
		}
	}

	/**
	 * Constructs an ObservableListContentProvider
	 */
	public ObservableListContentProvider() {
		impl = new Impl();
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		impl.inputChanged(viewer, oldInput, newInput);
	}

	public Object[] getElements(Object inputElement) {
		return impl.getElements(inputElement);
	}

	public void dispose() {
		impl.dispose();
	}

	/**
	 * Returns the set of elements known to this content provider. Label
	 * providers may track this set if they need to be notified about additions
	 * before the viewer sees the added element, and notified about removals
	 * after the element was removed from the viewer. This is intended for use
	 * by label providers, as it will always return the items that need labels.
	 * 
	 * @return readableSet of items that will need labels
	 */
	public IObservableSet getKnownElements() {
		return impl.getKnownElements();
	}

	/**
	 * Returns the set of known elements which have been realized in the viewer.
	 * Clients may track this set in order to perform custom actions on elements
	 * while they are known to be present in the viewer.
	 * 
	 * @return the set of known elements which have been realized in the viewer.
	 * @since 1.3
	 */
	public IObservableSet getRealizedElements() {
		return impl.getRealizedElements();
	}
}
