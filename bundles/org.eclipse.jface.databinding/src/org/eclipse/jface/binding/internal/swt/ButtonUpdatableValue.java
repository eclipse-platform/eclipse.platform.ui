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

package org.eclipse.jface.binding.internal.swt;

import org.eclipse.jface.binding.IChangeEvent;
import org.eclipse.jface.binding.UpdatableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @since 3.2
 * 
 */
public class ButtonUpdatableValue extends UpdatableValue {

	private final Button button;

	private Listener updateListener = new Listener() {
		public void handleEvent(Event event) {
			fireChangeEvent(IChangeEvent.CHANGE, null, new Boolean(button
					.getSelection()));
		}
	};

	/**
	 * @param button
	 * @param updatePolicy
	 */
	public ButtonUpdatableValue(Button button, int updatePolicy) {
		this.button = button;
		if (updatePolicy != SWT.None) {
			button.addListener(SWT.Selection, updateListener);
			button.addListener(SWT.DefaultSelection, updateListener);
		}
	}

	public void setValue(Object value) {
		button.setSelection(value == null ? false : ((Boolean) value)
				.booleanValue());
		fireChangeEvent(IChangeEvent.CHANGE, null, new Boolean(
				button.getSelection()));
	}

	public Object getValue() {
		return new Boolean(button.getSelection());
	}

	public Class getValueType() {
		return String.class;
	}

}
