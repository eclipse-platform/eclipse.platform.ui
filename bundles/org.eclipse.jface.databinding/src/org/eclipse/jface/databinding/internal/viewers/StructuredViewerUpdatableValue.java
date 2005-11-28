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

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.UpdatableValue;
import org.eclipse.jface.databinding.internal.swt.AsyncRunnable;
import org.eclipse.jface.databinding.internal.swt.SyncRunnable;
import org.eclipse.jface.databinding.viewers.ViewersProperties;
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
		if (attribute.equals(ViewersProperties.SINGLE_SELECTION)) {
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					if (!updating) {
						fireChangeEvent(ChangeEvent.CHANGE, null, getValue());
					}
				}
			});
		} else {
			throw new IllegalArgumentException(
					"Attribute name not valid: " + attribute); //$NON-NLS-1$
		}
	}

	public void setValue(final Object value) {
		AsyncRunnable runnable = new AsyncRunnable(){
			public void run(){
				try {
					updating = true;
					if (attribute.equals(ViewersProperties.SINGLE_SELECTION)) {
						Object oldValue= getValue();
						viewer.setSelection(value == null ? StructuredSelection.EMPTY
								: new StructuredSelection(value));
						fireChangeEvent(ChangeEvent.CHANGE, oldValue, getValue());
					}
				} finally {
					updating = false;
				}
			}
		};
		runnable.runOn(viewer.getControl().getDisplay());
	}

	public Object getValue() {
		SyncRunnable runnable = new SyncRunnable(){
			public Object run(){
				if (attribute.equals(ViewersProperties.SINGLE_SELECTION)) {
					ISelection selection = viewer.getSelection();
					if (selection instanceof IStructuredSelection) {
						IStructuredSelection sel = (IStructuredSelection) selection;
						return sel.getFirstElement();
					}
				}
				return null;				
			}
		};
		return runnable.runOn(viewer.getControl().getDisplay());
	}

	public Class getValueType() {
		Assert.isTrue(attribute.equals(ViewersProperties.SINGLE_SELECTION), "unexpected attribute: " + attribute); //$NON-NLS-1$
		return Object.class;
	}

}
