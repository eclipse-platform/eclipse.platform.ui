/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 215531)
 *     Matthew Hall - bug 226765
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

/**
 * NON-API - Abstract base class for content providers where the viewer input is
 * expected to be an {@link IObservableCollection}.
 * 
 * @since 1.2
 */
public abstract class ObservableCollectionContentProvider implements
		IStructuredContentProvider {
	private IObservableValue viewerObservable;

	/**
	 * Element comparer used by the viewer (may be null).
	 */
	protected IElementComparer comparer;

	/**
	 * Interface for sending updates to the viewer.
	 */
	protected ViewerUpdater viewerUpdater;

	/**
	 * Observable set of all elements known to the content provider. Subclasses
	 * must add new elements to this set <b>before</b> adding them to the
	 * viewer, and must remove old elements from this set <b>after</b> removing
	 * them from the viewer.
	 */
	protected IObservableSet knownElements;

	private IObservableSet unmodifiableKnownElements;
	private IObservableCollection observableCollection;

	/**
	 * Constructs an ObservableCollectionContentProvider
	 */
	protected ObservableCollectionContentProvider() {
		final Realm realm = SWTObservables.getRealm(Display.getDefault());
		viewerObservable = new WritableValue(realm);
		viewerUpdater = null;

		// Known elements is a detail set of viewerObservable, so that when we
		// get the viewer instance we can swap in a set that uses its
		// IElementComparer, if any.
		IObservableFactory knownElementsFactory = new IObservableFactory() {
			public IObservable createObservable(Object target) {
				IElementComparer comparer = null;
				if (target instanceof StructuredViewer)
					comparer = ((StructuredViewer) target).getComparer();
				return ObservableViewerElementSet.withComparer(realm, null,
						comparer);
			}
		};
		knownElements = MasterDetailObservables.detailSet(viewerObservable,
				knownElementsFactory, null);
		unmodifiableKnownElements = Observables
				.unmodifiableObservableSet(knownElements);

		observableCollection = null;
	}

	public Object[] getElements(Object inputElement) {
		if (observableCollection == null)
			return new Object[0];
		return observableCollection.toArray();
	}

	public void dispose() {
		if (observableCollection != null)
			removeCollectionChangeListener(observableCollection);

		if (viewerObservable != null) {
			viewerObservable.setValue(null);
			viewerObservable.dispose();
			viewerObservable = null;
		}
		viewerUpdater = null;
		knownElements = null;
		unmodifiableKnownElements = null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		setViewer(viewer);
		setInput(newInput);
	}

	private void setViewer(Viewer viewer) {
		viewerUpdater = createViewerUpdater(viewer);
		comparer = getElementComparer(viewer);
		viewerObservable.setValue(viewer); // (clears knownElements)
	}

	private static IElementComparer getElementComparer(Viewer viewer) {
		if (viewer instanceof StructuredViewer)
			return ((StructuredViewer) viewer).getComparer();
		return null;
	}

	ViewerUpdater createViewerUpdater(Viewer viewer) {
		if (viewer instanceof AbstractListViewer)
			return new ListViewerUpdater((AbstractListViewer) viewer);
		if (viewer instanceof AbstractTableViewer)
			return new TableViewerUpdater((AbstractTableViewer) viewer);
		throw new IllegalArgumentException(
				"This content provider only works with AbstractTableViewer or AbstractListViewer"); //$NON-NLS-1$
	}

	void setInput(Object input) {
		if (observableCollection != null) {
			removeCollectionChangeListener(observableCollection);
			observableCollection = null;
		}

		if (input != null) {
			checkInput(input);
			Assert.isTrue(input instanceof IObservableCollection,
					"Input must be an IObservableCollection"); //$NON-NLS-1$
			observableCollection = (IObservableCollection) input;
			addCollectionChangeListener(observableCollection);
			knownElements.addAll(observableCollection);
		}
	}

	/**
	 * Throws an exception if the input is not the correct type.
	 * 
	 * @param input
	 *            the input to check
	 */
	protected abstract void checkInput(Object input);

	/**
	 * Register for change event notification from the given collection.
	 * 
	 * @param collection
	 *            observable collection to listen to
	 */
	protected abstract void addCollectionChangeListener(
			IObservableCollection collection);

	/**
	 * Deregisters from change events notification on the given collection.
	 * 
	 * @param collection
	 *            observable collection to stop listening to
	 */
	protected abstract void removeCollectionChangeListener(
			IObservableCollection collection);

	/**
	 * Returns whether the viewer is disposed. Collection change listeners in
	 * subclasses should verify that the viewer is not disposed before sending
	 * any updates to the {@link ViewerUpdater viewer updater}.
	 * 
	 * @return whether the viewer is disposed.
	 */
	protected final boolean isViewerDisposed() {
		Viewer viewer = (Viewer) viewerObservable.getValue();
		return viewer == null || viewer.getControl() == null
				|| viewer.getControl().isDisposed();
	}

	/**
	 * Returns the set of elements known to this content provider. Label
	 * providers may track this set if they need to be notified about additions
	 * before the viewer sees the added element, and notified about removals
	 * after the element was removed from the viewer. This is intended for use
	 * by label providers, as it will always return the items that need labels.
	 * 
	 * @return unmodifiable observable set of items that will need labels
	 */
	public IObservableSet getKnownElements() {
		return unmodifiableKnownElements;
	}
}
