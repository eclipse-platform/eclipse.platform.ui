/*******************************************************************************
 * Copyright (c) 2007 Peter Centgraf and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Peter Centgraf - initial API and implementation, bug 124683
 *     Boris Bokowski, IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.internal.viewers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Observes multiple-selection of an {@link ISelectionProvider}.
 * 
 * @since 1.2
 */
public class SelectionProviderMultipleSelectionObservableList extends
		WritableList {

	protected ISelectionProvider selectionProvider;
	protected boolean handlingSelection;
	protected boolean updating;
	protected SelectionListener selectionListener = new SelectionListener();

	class SelectionListener implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			if (updating) {
				return;
			}
			handlingSelection = true;
			try {
				updateWrappedList(new ArrayList(getSelectionList(event.getSelection())));
			} finally {
				handlingSelection = false;
			}
		}
	}

	/**
	 * Create a new observable list based on the current selection of the given
	 * selection provider. Assumes that the selection provider provides
	 * structured selections.
	 * 
	 * @param realm
	 * @param selectionProvider
	 * @param elementType
	 */
	public SelectionProviderMultipleSelectionObservableList(Realm realm,
			ISelectionProvider selectionProvider, Object elementType) {
		super(realm, new ArrayList(getSelectionList(selectionProvider)), elementType);
		this.selectionProvider = selectionProvider;
		selectionProvider.addSelectionChangedListener(selectionListener);
	}

	protected void fireListChange(ListDiff diff) {
		if (handlingSelection) {
			super.fireListChange(diff);
		} else {
			// this is a bit of a hack - we are changing the diff to match the order
			// of elements returned by the selection provider after we've set the
			// selection.
			updating = true;
			try {
				List oldList = getSelectionList(selectionProvider);
				selectionProvider
						.setSelection(new StructuredSelection(wrappedList));
				wrappedList = new ArrayList(getSelectionList(selectionProvider));
				super.fireListChange(Diffs.computeListDiff(oldList, wrappedList));
			} finally {
				updating = false;
			}
		}
	}

	protected static List getSelectionList(ISelectionProvider selectionProvider) {
		if (selectionProvider == null) {
			throw new IllegalArgumentException();
		}
		return getSelectionList(selectionProvider.getSelection());
	}

	protected static List getSelectionList(ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			return ((IStructuredSelection) sel).toList();
		}
		return Collections.emptyList();
	}

	public synchronized void dispose() {
		selectionProvider.removeSelectionChangedListener(selectionListener);
		selectionProvider = null;
		super.dispose();
	}
}
