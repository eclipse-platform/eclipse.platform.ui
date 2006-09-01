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

package org.eclipse.jface.internal.databinding.provisional.viewers;

import java.util.Iterator;

import org.eclipse.jface.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.observable.set.ISetChangeListener;
import org.eclipse.jface.databinding.observable.set.SetDiff;

/**
 * @since 1.0
 * 
 */
public abstract class ListeningLabelProvider extends ViewerLabelProvider {

	private ISetChangeListener listener = new ISetChangeListener() {
		public void handleSetChange(IObservableSet source, SetDiff diff) {
			for (Iterator it = diff.getAdditions().iterator(); it.hasNext();) {
				addListenerTo(it.next());
			}
			for (Iterator it = diff.getRemovals().iterator(); it.hasNext();) {
				removeListenerFrom(it.next());
			}
		}
	};

	private IObservableSet items;

	/**
	 * @param itemsThatNeedLabels
	 */
	public ListeningLabelProvider(IObservableSet itemsThatNeedLabels) {
		this.items = itemsThatNeedLabels;
		items.addSetChangeListener(listener);
		for (Iterator it = items.iterator(); it.hasNext();) {
			addListenerTo(it.next());
		}
	}

	/**
	 * @param next
	 */
	protected abstract void removeListenerFrom(Object next);

	/**
	 * @param next
	 */
	protected abstract void addListenerTo(Object next);

	public void dispose() {
		for (Iterator iter = items.iterator(); iter.hasNext();) {
			removeListenerFrom(iter.next());
		}
		items.removeSetChangeListener(listener);
		super.dispose();
	}
}
