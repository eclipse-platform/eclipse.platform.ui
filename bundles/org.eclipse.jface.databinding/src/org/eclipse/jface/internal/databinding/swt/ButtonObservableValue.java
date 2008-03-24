/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *     Ashley Cambrell - bug 198904
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
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
						
			notifyIfChanged(oldSelectionValue, selectionValue);
		}
	};

	/**
	 * @param button
	 */
	public ButtonObservableValue(Button button) {
		super(button);
		this.button = button;
		init();
	}
	
	/**
	 * @param realm
	 * @param button
	 */
	public ButtonObservableValue(Realm realm, Button button) {
		super(realm, button);
		this.button = button;
		init();
	}
	
	private void init() {
		button.addListener(SWT.Selection, updateListener);
		button.addListener(SWT.DefaultSelection, updateListener);		
	}

	public void doSetValue(final Object value) {
		boolean oldSelectionValue = selectionValue;
		selectionValue = value == null ? false : ((Boolean) value)
				.booleanValue();
		
		button.setSelection(selectionValue);
		notifyIfChanged(oldSelectionValue, selectionValue);
	}

	public Object doGetValue() {
		return button.getSelection() ? Boolean.TRUE : Boolean.FALSE;
	}

	public Object getValueType() {
		return Boolean.TYPE;
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.databinding.observable.value.AbstractObservableValue#dispose()
	 */
	public synchronized void dispose() {
		super.dispose();

		if (!button.isDisposed()) {
			button.removeListener(SWT.Selection, updateListener);
			button.removeListener(SWT.DefaultSelection, updateListener);
		}
	}

	/**
	 * Notifies consumers with a value change event only if a change occurred.
	 * 
	 * @param oldValue
	 * @param newValue
	 */
	private void notifyIfChanged(boolean oldValue, boolean newValue) {
		if (oldValue != newValue) {
			fireValueChange(Diffs.createValueDiff(oldValue ? Boolean.TRUE : Boolean.FALSE,
					newValue ? Boolean.TRUE : Boolean.FALSE));
		}		
	}
}
