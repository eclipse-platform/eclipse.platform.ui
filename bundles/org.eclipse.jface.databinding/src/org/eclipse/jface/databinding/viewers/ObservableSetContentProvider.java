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
 *     Matthew Hall - bug 215531
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

/**
 * A {@link IStructuredContentProvider content provider} for
 * {@link AbstractTableViewer} or {@link AbstractListViewer} that provides
 * elements of an {@link IObservableSet} when set as the viewer's input. Objects
 * of this class listen for changes to the observable set, and will insert and
 * remove viewer elements to reflect observed changes.
 * 
 * <p>
 * This class is not intended to be subclassed by clients.
 * 
 * @since 1.1
 */
public class ObservableSetContentProvider extends
		ObservableCollectionContentProvider {
	/**
	 * Constructs an ObservableListContentProvider
	 */
	public ObservableSetContentProvider() {
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
	protected void checkInput(Object input) {
		Assert
				.isTrue(input instanceof IObservableSet,
						"This content provider only works with input of type IObservableSet"); //$NON-NLS-1$
	}

	ISetChangeListener changeListener = new ISetChangeListener() {
		public void handleSetChange(SetChangeEvent event) {
			if (isViewerDisposed())
				return;

			Set removals = event.diff.getRemovals();
			viewerUpdater.remove(removals.toArray());
			knownElements.removeAll(removals);

			Set additions = event.diff.getAdditions();
			knownElements.addAll(additions);
			viewerUpdater.add(additions.toArray());
		}
	};

	/**
	 * NON-API - This method is not public API, and may be changed or removed in
	 * the future. It is marked protected only so that it can be accessed from
	 * internal classes.
	 */
	protected void addCollectionChangeListener(IObservableCollection collection) {
		((IObservableSet) collection).addSetChangeListener(changeListener);
	}

	/**
	 * NON-API - This method is not public API, and may be changed or removed in
	 * the future. It is marked protected only so that it can be accessed from
	 * internal classes.
	 */
	protected void removeCollectionChangeListener(
			IObservableCollection collection) {
		((IObservableSet) collection).removeSetChangeListener(changeListener);
	}
}
