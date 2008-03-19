/*******************************************************************************
 * Copyright (c) 2007-2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 207858)
 *******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.IObservablesListener;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewerElementSet;
import org.eclipse.jface.internal.databinding.viewers.ObservableCollectionTreeContentProvider;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * An {@link ITreeContentProvider} for use with an {@link AbstractTreeViewer}, which
 * uses the provided {@link IObservableFactory list factory} to obtain the
 * elements of a tree. Object of this class listen for changes to each
 * {@link IObservableList} created by the factory, and will insert and remove
 * viewer elements to reflect the observed changes.
 * 
 * <p>
 * This class is not intended to be subclassed by clients.
 * 
 * @since 1.2
 */
public class ObservableListTreeContentProvider extends
		ObservableCollectionTreeContentProvider {
	/**
	 * Constructs an ObservableListTreeContentProvider using the given list
	 * factory.
	 * 
	 * @param listFactory
	 *            observable factory that produces an IObservableList of
	 *            children for a given parent element. Observable lists created
	 *            by this factory must be on the realm of the current display.
	 */
	public ObservableListTreeContentProvider(IObservableFactory listFactory) {
		super(listFactory);
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
		return super.getKnownElements();
	}

	/**
	 * NON-API - This method is not public API, and may be changed or removed in
	 * the future. It is marked protected only so that it can be accessed from
	 * internal classes.
	 */
	protected IObservablesListener createCollectionChangeListener(
			Object parentElement) {
		return new ListChangeListener(parentElement);
	}

	/**
	 * NON-API - This method is not public API, and may be changed or removed in
	 * the future. It is marked protected only so that it can be accessed from
	 * internal classes.
	 */
	protected void addCollectionChangeListener(
			IObservableCollection collection, IObservablesListener listener) {
		IObservableList list = (IObservableList) collection;
		IListChangeListener listListener = (IListChangeListener) listener;
		list.addListChangeListener(listListener);
	}

	/**
	 * NON-API - This method is not public API, and may be changed or removed in
	 * the future. It is marked protected only so that it can be accessed from
	 * internal classes.
	 */
	protected void removeCollectionChangeListener(
			IObservableCollection collection, IObservablesListener listener) {
		IObservableList list = (IObservableList) collection;
		IListChangeListener listListener = (IListChangeListener) listener;
		list.removeListChangeListener(listListener);
	}

	private class ListChangeListener implements IListChangeListener {
		final Object parentElement;

		public ListChangeListener(Object parentElement) {
			this.parentElement = parentElement;
		}

		public void handleListChange(ListChangeEvent event) {
			if (isViewerDisposed())
				return;

			final Set removals = ViewerElementSet.withComparer(comparer);
			event.diff.accept(new ListDiffVisitor() {
				public void handleAdd(int index, Object child) {
					// adds to known elements if new element
					getOrCreateNode(child).addParent(parentElement);

					viewerUpdater.insert(parentElement, child, index);
				}

				public void handleRemove(int index, Object child) {
					viewerUpdater.remove(parentElement, child, index);

					removals.add(child);
				}

				public void handleReplace(int index, Object oldChild,
						Object newChild) {
					getOrCreateNode(newChild).addParent(parentElement);

					viewerUpdater.replace(parentElement, oldChild, newChild,
							index);

					removals.add(oldChild);
				}

				public void handleMove(int oldIndex, int newIndex, Object child) {
					viewerUpdater.remove(parentElement, child, oldIndex);
					viewerUpdater.insert(parentElement, child, newIndex);
				}
			});

			// For each removed element, do not remove node's parent if the
			// element is still present elsewhere in the list.
			removals.removeAll(event.getObservableList());
			for (Iterator iterator = removals.iterator(); iterator.hasNext();) {
				TreeNode node = getExistingNode(iterator.next());
				if (node != null)
					// removes from known elements if last parent
					node.removeParent(parentElement);
			}
		}
	}
}