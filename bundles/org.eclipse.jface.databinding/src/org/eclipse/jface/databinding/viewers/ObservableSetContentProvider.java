/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Matthew Hall - bugs 215531, 226765, 222991, 238296, 266038
 *******************************************************************************/
package org.eclipse.jface.databinding.viewers;

import java.util.Set;

import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.internal.databinding.viewers.ObservableCollectionContentProvider;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A {@link IStructuredContentProvider content provider} for
 * {@link AbstractTableViewer} or {@link AbstractListViewer} that provides
 * elements of an {@link IObservableSet} when set as the viewer's input. Objects
 * of this class listen for changes to the observable set, and will insert and
 * remove viewer elements to reflect observed changes.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @since 1.1
 */
public class ObservableSetContentProvider implements IStructuredContentProvider {
	private ObservableCollectionContentProvider impl;

	private static class Impl extends ObservableCollectionContentProvider
			implements ISetChangeListener {
		protected Impl(IViewerUpdater explicitViewerUpdater) {
			super(explicitViewerUpdater);
		}

		protected void checkInput(Object input) {
			Assert
					.isTrue(input instanceof IObservableSet,
							"This content provider only works with input of type IObservableSet"); //$NON-NLS-1$
		}

		protected void addCollectionChangeListener(
				IObservableCollection collection) {
			((IObservableSet) collection).addSetChangeListener(this);
		}

		protected void removeCollectionChangeListener(
				IObservableCollection collection) {
			((IObservableSet) collection).removeSetChangeListener(this);
		}

		public void handleSetChange(SetChangeEvent event) {
			if (isViewerDisposed())
				return;

			Set removals = event.diff.getRemovals();
			Set additions = event.diff.getAdditions();

			knownElements.addAll(additions);
			if (realizedElements != null)
				realizedElements.removeAll(removals);

			viewerUpdater.remove(removals.toArray());
			viewerUpdater.add(additions.toArray());

			if (realizedElements != null)
				realizedElements.addAll(additions);
			knownElements.removeAll(removals);
		}
	}

	/**
	 * Constructs an ObservableSetContentProvider
	 */
	public ObservableSetContentProvider() {
		this(null);
	}

	/**
	 * Constructs an ObservableSetContentProvider with the given viewer updater
	 * 
	 * @param viewerUpdater
	 *            the viewer updater to use when elements are added or removed
	 *            from the input observable set.
	 * @since 1.3
	 */
	public ObservableSetContentProvider(IViewerUpdater viewerUpdater) {
		impl = new Impl(viewerUpdater);
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
	 * @return unmodifiable set of items that will need labels
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
