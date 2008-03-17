/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.AbstractTableViewer;

/**
 * This class can be used as a content provider for an
 * {@link AbstractTableViewer} or {@link AbstractListViewer} and will provide
 * elements of an {@link IObservableSet} when set as the viewer's input. Objects
 * of this class will listen for changes to the observable set and, based on the
 * observed changes, call corresponding update methods on the viewer.
 * 
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
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

	void checkInput(Object input) {
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

	void addCollectionChangeListener(IObservableCollection collection) {
		((IObservableSet) collection).addSetChangeListener(changeListener);
	}

	void removeCollectionChangeListener(IObservableCollection collection) {
		((IObservableSet) collection).removeSetChangeListener(changeListener);
	}
}
