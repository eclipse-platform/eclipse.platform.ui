/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.internal.viewers;

import org.eclipse.jface.internal.databinding.provisional.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.observable.value.AbstractObservableValue;
import org.eclipse.jface.internal.databinding.provisional.viewers.ViewersProperties;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * @since 3.2
 * 
 */
public class StructuredViewerObservableValue extends AbstractObservableValue {

	private final StructuredViewer viewer;

	private final String attribute;

	private boolean updating = false;

	private Object currentSelection;

	/**
	 * @param viewer
	 * @param attribute
	 */
	public StructuredViewerObservableValue(StructuredViewer viewer,
			String attribute) {
		this.viewer = viewer;
		this.attribute = attribute;
		this.currentSelection = doGetValue();
		if (attribute.equals(ViewersProperties.SINGLE_SELECTION)) {
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					if (!updating) {
						Object oldSelection = currentSelection;
						currentSelection = doGetValue();
						fireValueChange(Diffs.createValueDiff(oldSelection,
								currentSelection));
					}
				}
			});
		} else {
			throw new IllegalArgumentException(
					"Attribute name not valid: " + attribute); //$NON-NLS-1$
		}
	}

	public void setValue(final Object value) {
		try {
			updating = true;
			if (attribute.equals(ViewersProperties.SINGLE_SELECTION)) {
				Object oldSelection = currentSelection;
				viewer.setSelection(value == null ? StructuredSelection.EMPTY
						: new StructuredSelection(value));
				currentSelection = doGetValue();
				fireValueChange(Diffs.createValueDiff(oldSelection,
						currentSelection));
			}
		} finally {
			updating = false;
		}
	}

	public Object doGetValue() {
		if (attribute.equals(ViewersProperties.SINGLE_SELECTION)) {
			ISelection selection = viewer.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				return sel.getFirstElement();
			}
		}
		return null;
	}

	public Object getValueType() {
		Assert.isTrue(attribute.equals(ViewersProperties.SINGLE_SELECTION),
				"unexpected attribute: " + attribute); //$NON-NLS-1$
		return Object.class;
	}

}
