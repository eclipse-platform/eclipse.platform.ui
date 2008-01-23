/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - observeInput implementation (bug 206839)
 *******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.internal.viewers.SelectionProviderMultipleSelectionObservableList;
import org.eclipse.jface.internal.databinding.internal.viewers.SelectionProviderSingleSelectionObservableValue;
import org.eclipse.jface.internal.databinding.internal.viewers.ViewerInputObservableValue;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

/**
 * Factory methods for creating observables for JFace viewers
 * 
 * @since 1.1
 */
public class ViewersObservables {

	/**
	 * Returns an observable value that tracks the current selection of the
	 * given selection provider. If the selection provider provides selections
	 * of type {@link IStructuredSelection}, the observable value will be the
	 * first element of the structured selection as returned by
	 * {@link IStructuredSelection#getFirstElement()}.
	 * 
	 * @param selectionProvider
	 * @return the observable value tracking the (single) selection of the given
	 *         selection provider
	 */
	public static IObservableValue observeSingleSelection(
			ISelectionProvider selectionProvider) {
		return new SelectionProviderSingleSelectionObservableValue(
				SWTObservables.getRealm(Display.getDefault()),
				selectionProvider);
	}

	/**
	 * Returns an observable value that tracks the current selection of the
	 * given selection provider. Assumes that the selection provider provides
	 * selections of type {@link IStructuredSelection}. Note that the
	 * observable list will not honor the full contract of
	 * <code>java.util.List</code> in that it may delete or reorder elements
	 * based on what the selection provider returns from
	 * {@link ISelectionProvider#getSelection()} after having called
	 * {@link ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)}
	 * based on the requested change to the observable list. The affected
	 * methods are <code>add</code>, <code>addAll</code>, and
	 * <code>set</code>.
	 * 
	 * @param selectionProvider
	 * @return the observable value tracking the (multi) selection of the given
	 *         selection provider
	 * 
	 * @since 1.2
	 */
	public static IObservableList observeMultiSelection(
			ISelectionProvider selectionProvider) {
		return new SelectionProviderMultipleSelectionObservableList(
				SWTObservables.getRealm(Display.getDefault()),
				selectionProvider, Object.class);
	}

	/**
	 * Returns an observable value that tracks the input of the given viewer.
	 * <p>
	 * The returned observer is blind to changes in the viewer's input unless
	 * its {@link IObservableValue#setValue(Object)} method is called directly.
	 * 
	 * @param viewer
	 *            the viewer to observe
	 * @return an observable value tracking the input of the given viewer
	 * @since 1.2
	 */
	public static IObservableValue observeInput(Viewer viewer) {
		return new ViewerInputObservableValue(SWTObservables.getRealm(viewer
				.getControl().getDisplay()), viewer);
	}
}
