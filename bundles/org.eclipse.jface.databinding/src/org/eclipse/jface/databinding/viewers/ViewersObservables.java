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

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.internal.viewers.SelectionProviderSingleSelectionObservableValue;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.3
 * 
 */
public class ViewersObservables {

	/**
	 * @param selectionProvider
	 * @return
	 */
	public static IObservableValue observeSingleSelection(
			ISelectionProvider selectionProvider) {
		return new SelectionProviderSingleSelectionObservableValue(
				SWTObservables.getRealm(Display.getDefault()),
				selectionProvider);
	}

	// public IObservable createObservable(Object description) {
	// if (description instanceof Property) {
	// Object object = ((Property) description).getObject();
	// Object attribute = ((Property) description).getPropertyID();
	// if (object instanceof ISelectionProvider
	// && ViewersProperties.SINGLE_SELECTION.equals(attribute)) {
	// return new SelectionProviderSingleSelectionObservableValue(
	// (ISelectionProvider) object);
	// } else if (object instanceof AbstractListViewer
	// && ViewersProperties.CONTENT.equals(attribute)) {
	// return new AbstractListViewerObservableCollectionWithLabels(
	// (AbstractListViewer) object);
	// } else if (object instanceof TableViewer
	// && ViewersProperties.CONTENT.equals(attribute)) {
	// return new TableViewerObservableCollectionWithLabels(
	// (TableViewer) object);
	// }
	// } else if (description instanceof AbstractListViewer) {
	// return new AbstractListViewerObservableCollectionWithLabels(
	// (AbstractListViewer) description);
	// } else if (description instanceof TableViewer) {
	// return new TableViewerObservableCollectionWithLabels(
	// (TableViewer) description);
	// }
	// return null;
	// }

}
