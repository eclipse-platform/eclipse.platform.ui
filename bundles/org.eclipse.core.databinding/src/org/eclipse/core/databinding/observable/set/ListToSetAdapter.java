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

package org.eclipse.core.databinding.observable.set;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;

/**
 * Observable set backed by an observable list. The wrapped list must not
 * contain duplicate elements.
 * 
 * @since 3.2
 * 
 */
public class ListToSetAdapter extends ObservableSet {

	private final IObservableList list;

	private IListChangeListener listener = new IListChangeListener() {

		public void handleListChange(ListChangeEvent event) {
			Set added = new HashSet();
			Set removed = new HashSet();
			ListDiffEntry[] differences = event.diff.getDifferences();
			for (int i = 0; i < differences.length; i++) {
				ListDiffEntry entry = differences[i];
				Object element = entry.getElement();
				if (entry.isAddition()) {
					if (wrappedSet.add(element)) {
						if (!removed.remove(element))
							added.add(element);
					}
				} else {
					if (wrappedSet.remove(element)) {
						removed.add(element);
						added.remove(element);
					}
				}
			}
			fireSetChange(Diffs.createSetDiff(added, removed));
		}
	};

	/**
	 * @param list
	 */
	public ListToSetAdapter(IObservableList list) {
		super(list.getRealm(), new HashSet(), list.getElementType());
		this.list = list;
		wrappedSet.addAll(list);
		this.list.addListChangeListener(listener);
	}

	public void dispose() {
		super.dispose();
		if (list != null && listener != null) {
			list.removeListChangeListener(listener);
			listener = null;
		}
	}

}
