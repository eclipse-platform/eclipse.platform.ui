/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 207858)
 *******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.viewers;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.IObservablesListener;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.jface.internal.databinding.viewers.ObservableCollectionTreeContentProvider;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * An {@link ITreeContentProvider} for use with an {@link AbstractTreeViewer},
 * which uses the provided {@link IObservableFactory set factory} to obtain the
 * elements of a tree. Objects of this class listen for changes to each
 * {@link IObservableSet} created by the factory, and will insert and remove
 * viewer elements to reflect the observed changes.
 * 
 * <p>
 * This class is not intended to be subclassed by clients.
 * 
 * @since 1.2
 */
public class ObservableSetTreeContentProvider extends
		ObservableCollectionTreeContentProvider {
	/**
	 * Constructs an ObservableListTreeContentProvider using the given list
	 * factory.
	 * 
	 * @param setFactory
	 *            observable factory that produces an IObservableSet of children
	 *            for a given parent element. Observable sets created by this
	 *            factory must be on the realm of the current display.
	 */
	public ObservableSetTreeContentProvider(IObservableFactory setFactory) {
		super(setFactory);
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
		return new SetChangeListener(parentElement);
	}

	/**
	 * NON-API - This method is not public API, and may be changed or removed in
	 * the future. It is marked protected only so that it can be accessed from
	 * internal classes.
	 */
	protected void addCollectionChangeListener(
			IObservableCollection collection, IObservablesListener listener) {
		IObservableSet set = (IObservableSet) collection;
		ISetChangeListener setListener = (ISetChangeListener) listener;
		set.addSetChangeListener(setListener);
	}

	/**
	 * NON-API - This method is not public API, and may be changed or removed in
	 * the future. It is marked protected only so that it can be accessed from
	 * internal classes.
	 */
	protected void removeCollectionChangeListener(
			IObservableCollection collection, IObservablesListener listener) {
		IObservableSet set = (IObservableSet) collection;
		ISetChangeListener setListener = (ISetChangeListener) listener;
		set.removeSetChangeListener(setListener);
	}

	private class SetChangeListener implements ISetChangeListener {
		final Object parentElement;

		public SetChangeListener(Object parentElement) {
			this.parentElement = parentElement;
		}

		public void handleSetChange(SetChangeEvent event) {
			if (isViewerDisposed())
				return;

			Set removals = event.diff.getRemovals();
			if (!removals.isEmpty()) {
				viewerUpdater.remove(parentElement, removals.toArray());
				for (Iterator iterator = removals.iterator(); iterator
						.hasNext();) {
					Object child = iterator.next();
					TreeNode childNode = getExistingNode(child);
					if (childNode != null)
						childNode.removeParent(parentElement);
				}
			}

			Set additions = event.diff.getAdditions();
			if (!additions.isEmpty()) {
				for (Iterator iterator = additions.iterator(); iterator
						.hasNext();) {
					Object child = iterator.next();
					getOrCreateNode(child).addParent(parentElement);
				}
				viewerUpdater.add(parentElement, additions.toArray());
			}
		}
	}
}