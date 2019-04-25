/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewerLabelProvider;

/**
 * @param <E> type of the viewer elements that labels are provided for
 *
 * @since 1.1
 *
 */
public abstract class ListeningLabelProvider<E> extends ViewerLabelProvider {

	private ISetChangeListener<E> listener = event -> {
		for (E element : event.diff.getAdditions()) {
			addListenerTo(element);
		}
		for (E element : event.diff.getRemovals()) {
			removeListenerFrom(element);
		}
	};

	private IObservableSet<E> items;

	/**
	 * @param itemsThatNeedLabels
	 */
	public ListeningLabelProvider(IObservableSet<E> itemsThatNeedLabels) {
		this.items = itemsThatNeedLabels;
		items.addSetChangeListener(listener);
		for (E element : items) {
			addListenerTo(element);
		}
	}

	/**
	 * @param next
	 */
	protected abstract void removeListenerFrom(E next);

	/**
	 * @param next
	 */
	protected abstract void addListenerTo(E next);

	@Override
	public void dispose() {
		for (E element : items) {
			removeListenerFrom(element);
		}
		items.removeSetChangeListener(listener);
		super.dispose();
	}
}
