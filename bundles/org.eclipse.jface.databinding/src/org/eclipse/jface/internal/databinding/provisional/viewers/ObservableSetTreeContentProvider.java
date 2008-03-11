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
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * NON-API - An {@link ITreeContentProvider} which uses a
 * {@link IObservableFactory set factory} to obtain the elements of a tree. Each
 * observable set obtained from the factory is observed such that changes in the
 * set are reflected in the viewer.
 * 
 * @since 1.2
 */
public class ObservableSetTreeContentProvider extends
		ObservableCollectionTreeContentProvider {
	/**
	 * Constructs an ObservableSetTreeContentProvider using the given parent
	 * provider and set factory.
	 * 
	 * @param parentProvider
	 *            parent provider
	 * @param setFactory
	 *            observable factory that produces an IObservableSet of children
	 *            for a given parent element.
	 */
	public ObservableSetTreeContentProvider(IParentProvider parentProvider,
			IObservableFactory setFactory) {
		super(parentProvider, setFactory);
	}

	/**
	 * Constructs an ObservableListTreeContentProvider using the given list
	 * factory.
	 * 
	 * @param setFactory
	 *            observable factory that produces an IObservableSet of children
	 *            for a given parent element.
	 */
	public ObservableSetTreeContentProvider(IObservableFactory setFactory) {
		this(null, setFactory);
	}

	protected IObservablesListener createCollectionChangeListener(
			Object parentElement) {
		return new SetChangeListener(parentElement);
	}

	protected void addCollectionChangeListener(
			IObservableCollection collection, IObservablesListener listener) {
		IObservableSet set = (IObservableSet) collection;
		ISetChangeListener setListener = (ISetChangeListener) listener;
		set.addSetChangeListener(setListener);
	}

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