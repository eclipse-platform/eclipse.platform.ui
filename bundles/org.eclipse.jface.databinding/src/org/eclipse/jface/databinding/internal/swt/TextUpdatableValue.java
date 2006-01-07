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
package org.eclipse.jface.databinding.internal.swt;

import org.eclipse.jface.databinding.IChangeEvent;
import org.eclipse.jface.databinding.UpdatableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.2
 *
 */
public class TextUpdatableValue extends UpdatableValue {

	private final Text text;

	private boolean updating = false;

	private Listener validateListener = new Listener() {
		public void handleEvent(Event event) {
			if (!updating) {
				fireChangeEvent(IChangeEvent.CHANGE, null, text.getText());
			}
		}
	};

	private Listener updateListener = new Listener() {
		public void handleEvent(Event event) {
			if (!updating) {
				fireChangeEvent(IChangeEvent.CHANGE, null, text.getText());
			}
		}
	};

	/**
	 * @param text
	 * @param validatePolicy
	 * @param updatePolicy
	 */
	public TextUpdatableValue(Text text, int validatePolicy, int updatePolicy) {
		this.text = text;
		if (updatePolicy != SWT.None) {
			text.addListener(updatePolicy, updateListener);
		}
		text.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				if (!updating) {
					String currentText = TextUpdatableValue.this.text.getText();
					String newText = currentText.substring(0, e.start) + e.text
							+ currentText.substring(e.end);
					IChangeEvent changeEvent = fireChangeEvent( 
							IChangeEvent.VERIFY, currentText, newText);
					if (changeEvent.getVeto()) {
						e.doit = false;
					}
				}
			}
		});
	}

	public void setValue(Object value) {
		String oldValue = text.getText();
		try {
			updating = true;
			text.setText(value == null ? "" : value.toString()); //$NON-NLS-1$
		} finally {
			updating = false;
		}
		fireChangeEvent(IChangeEvent.CHANGE, oldValue, text.getText());
	}

	public Object getValue() {
		return text.getText();
	}

	public Class getValueType() {
		return String.class;
	}

}
