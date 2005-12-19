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
package org.eclipse.jface.databinding.internal.swt;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.UpdatableValue;
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
			fireChangeEvent(ChangeEvent.CHANGE, null, new Boolean(button
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

	public void setValue(final Object value) {
		AsyncRunnable runnable = new AsyncRunnable(){
			public void run(){
				button.setSelection(value == null ? false : ((Boolean) value)
						.booleanValue());
				fireChangeEvent(ChangeEvent.CHANGE, null, new Boolean(
						button.getSelection()));				
			}
		};
		runnable.runOn(button.getDisplay());
	}

	public Object getValue() {
		SyncRunnable runnable = new SyncRunnable(){
			public Object run(){
				return new Boolean(button.getSelection());				
			}
		};
		return runnable.runOn(button.getDisplay());
	}

	public Class getValueType() {
		return boolean.class;
	}

}
