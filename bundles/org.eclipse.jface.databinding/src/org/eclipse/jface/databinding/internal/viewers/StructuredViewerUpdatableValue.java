/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.internal.viewers;

import org.eclipse.jface.databinding.DataBinding;
import org.eclipse.jface.databinding.IChangeEvent;
import org.eclipse.jface.databinding.UpdatableValue;
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
public class StructuredViewerUpdatableValue extends UpdatableValue {

	private final StructuredViewer viewer;

	private final String attribute;

	private boolean updating = false;

	/**
	 * @param viewer
	 * @param attribute
	 */
	public StructuredViewerUpdatableValue(StructuredViewer viewer,
			String attribute) {
		this.viewer = viewer;
		this.attribute = attribute;
		if (attribute.equals(DataBinding.SELECTION)) {
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					if (!updating) {
						fireChangeEvent(IChangeEvent.CHANGE, null, getValue());
					}
				}
			});
		} else {
			throw new IllegalArgumentException(
					"Attribute name not valid: " + attribute); //$NON-NLS-1$
		}
	}

	public void setValue(Object value) {
		try {
			updating = true;
			if (attribute.equals(DataBinding.SELECTION)) {
				Object oldValue= getValue();
				viewer.setSelection(value == null ? StructuredSelection.EMPTY
						: new StructuredSelection(value));
				fireChangeEvent(IChangeEvent.CHANGE, oldValue, getValue());
			}
		} finally {
			updating = false;
		}
	}

	public Object getValue() {
		if (attribute.equals(DataBinding.SELECTION)) {
			ISelection selection = viewer.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				return sel.getFirstElement();
			}
		}
		return null;
	}

	public Class getValueType() {
		if (attribute.equals(DataBinding.SELECTION)) {
			return Object.class;
		}
		throw new AssertionError("unexpected attribute"); //$NON-NLS-1$
	}

}
