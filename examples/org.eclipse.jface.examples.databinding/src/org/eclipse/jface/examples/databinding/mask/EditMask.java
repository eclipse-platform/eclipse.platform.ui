/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.mask;

import java.beans.PropertyChangeSupport;

import org.eclipse.jface.examples.databinding.mask.internal.EditMaskParser;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * Ensures text widget has the format specified by the edit mask.
 * 
 * Algorithm:
 * 
 * We always let the verify event pass if the user typed a new digit or selected/deleted anything.
 * During the verify event, we post an async runnable.
 * Inside that async runnable, we:
 *   - Remember the selection position
 *   - getText(), then
 *   - Strip out all special characters
 *   - Truncate the resulting string to 10 digits
 *   - Re-add special characters at the correct positions
 *   - setText() the resulting string
 *   - reset the selection to the correct location
 *   
 * @since 3.3
 */
public class EditMask {

	private static final String FIELD_TEXT = "text";
	private static final String FIELD_RAW_TEXT = "rawText";
	protected Text text;
	protected EditMaskParser editMaskParser;
	private PropertyChangeSupport propertyChangeSupport;
	
	/**
	 * Creates an instance that wraps around a text widget and manage its<br>
	 * formatting.
	 * 
	 * @param text
	 * @param editMask
	 */
	public EditMask(Text text, String editMask) {
		this.text = text;
		editMaskParser = new EditMaskParser(editMask);
		updateTextField.run();
		text.addVerifyListener(verifyListener);
		text.addDisposeListener(disposeListener);
		text.addFocusListener(focusListener);
	}

    public void setText(String string) {
    	String oldValue = text.getText();
		editMaskParser.setInput(string);
		text.setText(editMaskParser.getFormattedResult());
		firePropertyChange(FIELD_TEXT, oldValue, string);
	}

	/**
	 * @return the actual(formatted) text
	 */
	public String getText() {
		return editMaskParser.getFormattedResult();
	}
	
	/**
	 * setRawText takes raw text as a parameter but formats it before
	 * setting the text in the Text control.
	 * 
	 * @param string the raw (unformatted) text
	 */
	public void setRawText(String string)  {
		String oldValue = editMaskParser.getRawResult();
		editMaskParser.setInput(string);
		text.setText(editMaskParser.getFormattedResult());
		firePropertyChange(FIELD_RAW_TEXT, oldValue, string);
	}

	/**
	 * @return The input text with literals removed
	 */
	public String getRawText() {
		return editMaskParser.getRawResult();
	}

	private PropertyChangeSupport getPropertyChangeSupport() {
		if (propertyChangeSupport == null) {
			propertyChangeSupport = new PropertyChangeSupport(this);
		}
		return propertyChangeSupport;
	}

	private boolean isEitherValueNotNull(Object oldValue, Object newValue) {
		return oldValue != null || newValue != null;
	}

	private void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		if (isEitherValueNotNull(oldValue, newValue)) {
			getPropertyChangeSupport().firePropertyChange(propertyName,
					oldValue, newValue);
		}
	}

	protected boolean updating = false;

	protected int oldSelection = 0;
	protected int selection = 0;
	
	private VerifyListener verifyListener = new VerifyListener() {
		public void verifyText(VerifyEvent e) {
			oldSelection = selection;
			selection = text.getCaretPosition();
			if (!updating)
				Display.getCurrent().asyncExec(updateTextField);
		}
	};

	private Runnable updateTextField = new Runnable() {
		public void run() {
			updating = true;
			try {
				editMaskParser.setInput(text.getText());
				text.setText(editMaskParser.getFormattedResult());
				if (selection > oldSelection) {
					int oldSelectionDelta = 
						editMaskParser.getNextInputPosition(oldSelection)
							- oldSelection;
					selection += oldSelectionDelta;
				}
				text.setSelection(new Point(selection, selection));
			} finally {
				updating = false;
			}
		}
	};
	
	private FocusListener focusListener = new FocusListener() {

		public void focusGained(FocusEvent e) {
			selection = editMaskParser.getNextInputPosition(0);
			text.setSelection(selection, selection);
		}

		public void focusLost(FocusEvent e) {
			// no op
		}
	};
	
	private DisposeListener disposeListener = new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			text.removeVerifyListener(verifyListener);
			text.removeDisposeListener(disposeListener);
		}
	};
}
