/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @since 1.0
 * 
 */
public class ButtonObservableValue extends AbstractSWTObservableValue {

	private final Button button;

	private boolean selectionValue;

	private Listener updateListener = new Listener() {
		public void handleEvent(Event event) {
			boolean oldSelectionValue = selectionValue;
			selectionValue = button.getSelection();
			fireValueChange(Diffs.createValueDiff(oldSelectionValue ? Boolean.TRUE : Boolean.FALSE,
					selectionValue ? Boolean.TRUE : Boolean.FALSE));
		}
	};

	/**
	 * @param button
	 */
	public ButtonObservableValue(Button button) {
		super(button);
		this.button = button;
		button.addListener(SWT.Selection, updateListener);
		button.addListener(SWT.DefaultSelection, updateListener);
	}

	public void doSetValue(final Object value) {
		boolean oldSelectionValue = selectionValue;
		selectionValue = value == null ? false : ((Boolean) value)
				.booleanValue();
		button.setSelection(selectionValue);
		fireValueChange(Diffs.createValueDiff(oldSelectionValue ? Boolean.TRUE : Boolean.FALSE,
				selectionValue ? Boolean.TRUE : Boolean.FALSE));
	}

	public Object doGetValue() {
		return button.getSelection() ? Boolean.TRUE : Boolean.FALSE;
	}

	public Object getValueType() {
		return Boolean.class;
	}

}
