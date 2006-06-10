/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.jface.internal.databinding.provisional.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.observable.value.AbstractVetoableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * @since 1.0
 * 
 */
public class TextObservableValue extends AbstractVetoableValue {

	private final Text text;

	private boolean updating = false;

	private int updatePolicy;

	private String bufferedValue;

	private Listener updateListener = new Listener() {
		public void handleEvent(Event event) {
			if (!updating) {
				String oldValue = bufferedValue;
				String newValue = text.getText();

				// If we are updating on focus lost then when we fire the change
				// event change the buffered value
				if (updatePolicy == SWT.FocusOut) {
					bufferedValue = text.getText();
					
					if (!newValue.equals(oldValue)) {
						fireValueChange(Diffs.createValueDiff(oldValue,
								newValue));
					}
				} else {
					fireValueChange(Diffs.createValueDiff(oldValue, text
							.getText()));
				}
			}
		}
	};

	private VerifyListener verifyListener;
	private KeyListener keyListener;
	private ShellListener shellListener;

	/**
	 * @param text
	 * @param updatePolicy
	 */
	public TextObservableValue(final Text text, int updatePolicy) {
		this.text = text;
		this.updatePolicy = updatePolicy;
		if (updatePolicy != SWT.None) {
			text.addListener(updatePolicy, updateListener);
		}
		// If the update policy is TIME_EARLY then the model is notified of
		// changed on key stroke by key stroke
		// When escape is pressed we need to rollback to the previous value
		// which is done with a key listener, however
		// the bufferedValue (the last remembered change value) must be changed
		// on focus lost
		if (updatePolicy == SWT.Modify) {
			text.addListener(SWT.FocusOut, new Listener() {
				public void handleEvent(Event event) {
					if (!updating) {
						bufferedValue = text.getText();
					}
				}
			});
		}
		verifyListener = new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				if (!updating) {
					String currentText = TextObservableValue.this.text
							.getText();
					String newText = currentText.substring(0, e.start) + e.text
							+ currentText.substring(e.end);
					if (!fireValueChanging(Diffs.createValueDiff(currentText,
							newText))) {
						e.doit = false;
					}
				}
			}
		};
		text.addVerifyListener(verifyListener);
		keyListener = new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.ESC && bufferedValue != null) {
					// Revert the value in the text field to the model value
					text.setText(bufferedValue);
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		};
		text.addKeyListener(keyListener);
		shellListener = new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				if (!text.isDisposed()) {
					String oldValue = bufferedValue;
					String newValue = text.getText();
	
					if (!newValue.equals(oldValue)) {
						fireValueChange(Diffs.createValueDiff(oldValue,
								newValue));
					}
				}
			}
		};
		text.getShell().addShellListener(shellListener);
	}

	public void doSetValue(final Object value) {
		try {
			updating = true;
			bufferedValue = (String) value;
			text.setText(value == null ? "" : value.toString()); //$NON-NLS-1$
		} finally {
			updating = false;
		}
	}

	public Object doGetValue() {
		return text.getText();
	}

	public Object getValueType() {
		return String.class;
	}

	public void dispose() {
		if (!text.isDisposed()) {
			text.removeKeyListener(keyListener);
			if (updatePolicy != SWT.None) {
				text.removeListener(updatePolicy, updateListener);
			}
			text.removeVerifyListener(verifyListener);
			text.getShell().removeShellListener(shellListener);
		}
		super.dispose();
	}
}
