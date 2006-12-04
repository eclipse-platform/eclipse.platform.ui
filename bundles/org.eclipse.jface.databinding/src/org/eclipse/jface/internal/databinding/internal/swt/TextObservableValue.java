/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds (bug 135446)
 *     Brad Reynolds - bug 164653
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTVetoableValue;
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
 * {@link IObservable} implementation that wraps a {@link Text} widget. The time
 * at which listeners should be notified about changes to the text is specified
 * on construction.
 * 
 * <dl>
 * <dt>Events:</dt>
 * <dd> If the update event type (specified on construction) is
 * <code>SWT.Modify</code> a value change event will be fired on every key
 * stroke. If the update event type is <code>SWT.FocusOut</code> a value
 * change event will be fired on focus out. When in either mode if the user is
 * entering text and presses [Escape] the value will be reverted back to the
 * last value set using doSetValue(). Regardless of the update event type a
 * value changing event will fire on verify to enable vetoing of changes.</dd>
 * </dl>
 * 
 * @since 1.0
 */
public class TextObservableValue extends AbstractSWTVetoableValue {

	/**
	 * {@link Text} widget that this is being observed.
	 */
	private final Text text;

	/**
	 * Flag to track when the model is updating the widget. When
	 * <code>true</code> the handlers for the SWT events should not process
	 * the event as this would cause an infinite loop.
	 */
	private boolean updating = false;

	/**
	 * SWT event that on firing this observable will fire change events to its
	 * listeners.
	 */
	private int updateEventType;

	/**
	 * Valid types for the {@link #updateEventType}.
	 */
	private static final int[] validUpdateEventTypes = new int[] { SWT.Modify,
			SWT.FocusOut, SWT.NONE };

	/**
	 * Last value set using doSetValue(), or null. This is maintained so that
	 * when entering text if the consumer were to press [Escape] the value can
	 * be reverted back to the last known externally-set value.
	 */
	private String bufferedValue;

	private Listener updateListener = new Listener() {
		public void handleEvent(Event event) {
			if (!updating) {
				String oldValue = bufferedValue;
				String newValue = text.getText();

				// If we are updating on focus lost then when we fire the change
				// event change the buffered value
				if (updateEventType == SWT.FocusOut) {
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
	 * Constructs a new instance bound to the given <code>text</code> widget
	 * and configured to fire change events to its listeners at the time of the
	 * <code>updateEventType</code>.
	 * 
	 * @param text
	 * @param updateEventType
	 *            SWT event constant as to what SWT event to update the model in
	 *            response to. Appropriate values are: <code>SWT.Modify</code>,
	 *            <code>SWT.FocusOut</code>, <code>SWT.NONE</code>.
	 * @throws IllegalArgumentException
	 *             if <code>updateEventType</code> is an incorrect type.
	 */
	public TextObservableValue(final Text text, int updateEventType) {
		super(text);
		boolean eventValid = false;
		for (int i = 0; !eventValid && i < validUpdateEventTypes.length; i++) {
			eventValid = (updateEventType == validUpdateEventTypes[i]);
		}
		if (!eventValid) {
			throw new IllegalArgumentException(
					"UpdateEventType [" + updateEventType + "] is not supported."); //$NON-NLS-1$//$NON-NLS-2$
		}
		this.text = text;
		this.updateEventType = updateEventType;
		if (updateEventType != SWT.None) {
			text.addListener(updateEventType, updateListener);
		}
		// If the update policy is SWT.Modify then the model is notified of
		// changed on key stroke by key stroke
		// When escape is pressed we need to rollback to the previous value
		// which is done with a key listener, however
		// the bufferedValue (the last remembered change value) must be changed
		// on focus lost
		if (updateEventType == SWT.Modify) {
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

	/**
	 * Sets the bound {@link Text Text's} text to the passed <code>value</code>.
	 * 
	 * @param value
	 *            new value, String expected
	 * @see org.eclipse.core.databinding.observable.value.AbstractVetoableValue#doSetApprovedValue(java.lang.Object)
	 * @throws ClassCastException
	 *             if the value is anything other than a String
	 */
	public void doSetApprovedValue(final Object value) {
		try {
			updating = true;
			bufferedValue = (String) value;
			text.setText(value == null ? "" : value.toString()); //$NON-NLS-1$
		} finally {
			updating = false;
		}
	}

	/**
	 * Returns the current value of the {@link Text}.
	 * 
	 * @see org.eclipse.core.databinding.observable.value.AbstractVetoableValue#doGetValue()
	 */
	public Object doGetValue() {
		return text.getText();
	}

	/**
	 * Returns the type of the value from {@link #doGetValue()}, i.e.
	 * String.class
	 * 
	 * @see org.eclipse.core.databinding.observable.value.IObservableValue#getValueType()
	 */
	public Object getValueType() {
		return String.class;
	}

	public void dispose() {
		if (!text.isDisposed()) {
			text.removeKeyListener(keyListener);
			if (updateEventType != SWT.None) {
				text.removeListener(updateEventType, updateListener);
			}
			text.removeVerifyListener(verifyListener);
			text.getShell().removeShellListener(shellListener);
		}
		super.dispose();
	}
}
