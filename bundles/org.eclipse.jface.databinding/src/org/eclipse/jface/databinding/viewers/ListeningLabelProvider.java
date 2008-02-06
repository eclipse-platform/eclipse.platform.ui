/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import java.util.Iterator;

import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewerLabelProvider;

/**
 * @since 1.1
 * 
 */
public abstract class ListeningLabelProvider extends ViewerLabelProvider {

	private ISetChangeListener listener = new ISetChangeListener() {
		public void handleSetChange(SetChangeEvent event) {
			for (Iterator it = event.diff.getAdditions().iterator(); it.hasNext();) {
				addListenerTo(it.next());
			}
			for (Iterator it = event.diff.getRemovals().iterator(); it.hasNext();) {
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
