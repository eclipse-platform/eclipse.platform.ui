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
import org.eclipse.jface.binding.IChangeListener;
import org.eclipse.jface.binding.UpdatableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
			fireChangeEvent(null, IChangeEvent.CHANGE, null, new Boolean(button
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
		button.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				System.out.println("selected"); //$NON-NLS-1$
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				System.out.println("defaultselected"); //$NON-NLS-1$
			}
		});
	}

	public void setValue(Object value, IChangeListener listenerToOmit) {
		button.setSelection(value == null ? false : ((Boolean) value)
				.booleanValue()); //$NON-NLS-1$
		fireChangeEvent(listenerToOmit, IChangeEvent.CHANGE, null, new Boolean(
				button.getSelection()));
	}

	public Object getValue() {
		return new Boolean(button.getSelection());
	}

	public Class getValueType() {
		return String.class;
	}

}
