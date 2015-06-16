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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.jface.examples.databinding.mask.internal.EditMaskParser;
import org.eclipse.jface.examples.databinding.mask.internal.SWTUtil;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * Ensures text widget has the format specified by the edit mask.  Edit masks
 * are currently defined as follows:
 *
 * The following characters are reserved words that match specific kinds of
 * characters:
 *
 * # - digits 0-9
 * A - uppercase A-Z
 * a - upper or lowercase a-z, A-Z
 * n - alphanumeric 0-9, a-z, A-Z
 *
 * All other characters are literals.  The above characters may be turned into
 * literals by preceeding them with a backslash.  Use two backslashes to
 * denote a backslash.
 *
 * Examples:
 *
 * (###) ###-####  U.S. phone number
 * ###-##-####     U.S. Social Security number
 * \\\###          A literal backslash followed by a literal pound symbol followed by two digits
 *
 * Ideas for future expansion:
 *
 * Quantifiers as postfix modifiers to a token.  ie:
 *
 * #{1, 2}/#{1,2}/####   MM/DD/YYYY date format allowing 1 or 2 digit month or day
 *
 * Literals may only be quantified as {0,1} which means that they only appear
 * if placeholders on both sides of the literal have data.  This will be used
 * along with:
 *
 * Right-to-left support for numeric entry.  When digits are being entered and
 * a decimal point is present in the mask, digits to the left of the decimal
 * are entered right-to-left but digits to the right of the decimal left-to-right.
 * This might need to be a separate type of edit mask. (NumericMask, maybe?)
 *
 * Example:
 *
 * $#{0,3},{0,1}#{0,3}.#{0,2}  ie: $123,456.12 or $12.12 or $1,234.12 or $123.12
 *
 *
 * Here's the basic idea of how the current implementation works (the actual
 * implementation is slightly more abstracted and complicated than this):
 *
 * We always let the verify event pass if the user typed a new character or selected/deleted anything.
 * During the verify event, we post an async runnable.
 * Inside that async runnable, we:
 *   - Remember the selection position
 *   - getText(), then
 *   - Strip out all literal characters from text
 *   - Truncate the resulting string to raw length of edit mask without literals
 *   - Insert literal characters back in the correct positions
 *   - setText() the resulting string
 *   - reset the selection to the correct location
 *
 * @since 3.3
 */
public class EditMask {

	public static final String FIELD_TEXT = "text";
	public static final String FIELD_RAW_TEXT = "rawText";
	public static final String FIELD_COMPLETE = "complete";
	protected Text text;
	protected EditMaskParser editMaskParser;
	private boolean fireChangeOnKeystroke = true;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	protected String oldValidRawText = "";
	protected String oldValidText = "";

	/**
	 * Creates an instance that wraps around a text widget and manages its<br>
	 * formatting.
	 *
	 * @param text
	 * @param editMask
	 */
	public EditMask(Text text) {
		this.text = text;
	}

	/**
	 * @return the underlying Text control used by EditMask
	 */
	public Text getControl() {
		return this.text;
	}

	/**
	 * Set the edit mask string on the edit mask control.
	 *
	 * @param editMask The edit mask string
	 */
	public void setMask(String editMask) {
		editMaskParser = new EditMaskParser(editMask);
		text.addVerifyListener(verifyListener);
		text.addFocusListener(focusListener);
		text.addDisposeListener(disposeListener);
		updateTextField.run();
		oldValidText = text.getText();
		oldValidRawText = editMaskParser.getRawResult();
	}

    /**
     * @param string Sets the text string in the receiver
     */
    public void setText(String string) {
    	String oldValue = text.getText();
    	if (editMaskParser != null) {
			editMaskParser.setInput(string);
			String formattedResult = editMaskParser.getFormattedResult();
			text.setText(formattedResult);
			firePropertyChange(FIELD_TEXT, oldValue, formattedResult);
    	} else {
    		text.setText(string);
			firePropertyChange(FIELD_TEXT, oldValue, string);
    	}
		oldValidText = text.getText();
		oldValidRawText = editMaskParser.getRawResult();
	}

	/**
	 * @return the actual (formatted) text
	 */
	public String getText() {
		if (editMaskParser != null) {
			return editMaskParser.getFormattedResult();
		}
		return text.getText();
	}

	/**
	 * setRawText takes raw text as a parameter but formats it before
	 * setting the text in the Text control.
	 *
	 * @param string the raw (unformatted) text
	 */
	public void setRawText(String string)  {
		if (string == null) {
			string = "";
		}
		if (editMaskParser != null) {
			String oldValue = editMaskParser.getRawResult();
			editMaskParser.setInput(string);
			text.setText(editMaskParser.getFormattedResult());
			firePropertyChange(FIELD_RAW_TEXT, oldValue, string);
		} else {
	    	String oldValue = text.getText();
    		text.setText(string);
			firePropertyChange(FIELD_RAW_TEXT, oldValue, string);
		}
		oldValidText = text.getText();
		oldValidRawText = editMaskParser.getRawResult();
	}

	/**
	 * @return The input text with literals removed
	 */
	public String getRawText() {
		if (editMaskParser != null) {
			return editMaskParser.getRawResult();
		}
		return text.getText();
	}

	/**
	 * @return true if the field is complete according to the mask; false otherwise
	 */
	public boolean isComplete() {
		if (editMaskParser == null) {
			return true;
		}
		return editMaskParser.isComplete();
	}

	/**
	 * Returns the placeholder character.  The placeholder
	 * character must be a different character than any character that is
	 * allowed as input anywhere in the edit mask.  For example, if the edit
	 * mask permits spaces to be used as input anywhere, the placeholder
	 * character must be something other than a space character.
	 * <p>
	 * The space character is the default placeholder character.
	 *
	 * @return the placeholder character
	 */
	public char getPlaceholder() {
		if (editMaskParser == null) {
			throw new IllegalArgumentException("Have to set an edit mask first");
		}
		return editMaskParser.getPlaceholder();
	}

	/**
	 * Sets the placeholder character for the edit mask.  The placeholder
	 * character must be a different character than any character that is
	 * allowed as input anywhere in the edit mask.  For example, if the edit
	 * mask permits spaces to be used as input anywhere, the placeholder
	 * character must be something other than a space character.
	 * <p>
	 * The space character is the default placeholder character.
	 *
	 * @param placeholder The character to use as a placeholder
	 */
	public void setPlaceholder(char placeholder) {
		if (editMaskParser == null) {
			throw new IllegalArgumentException("Have to set an edit mask first");
		}
		editMaskParser.setPlaceholder(placeholder);
		updateTextField.run();
		oldValidText = text.getText();
	}

	/**
	 * Indicates if change notifications will be fired after every keystroke
	 * that affects the value of the rawText or only when the value is either
	 * complete or empty.
	 *
	 * @return true if every change (including changes from one invalid state to
	 *         another) triggers a change event; false if only empty or valid
	 *         values trigger a change event.  Defaults to false.
	 */
	public boolean isFireChangeOnKeystroke() {
		return fireChangeOnKeystroke;
	}

	/**
	 * Sets if change notifications will be fired after every keystroke that
	 * affects the value of the rawText or only when the value is either
	 * complete or empty.
	 *
	 * @param fireChangeOnKeystroke
	 *            true if every change (including changes from one invalid state
	 *            to another) triggers a change event; false if only empty or
	 *            valid values trigger a change event.  Defaults to false.
	 */
	public void setFireChangeOnKeystroke(boolean fireChangeOnKeystroke) {
		this.fireChangeOnKeystroke = fireChangeOnKeystroke;
	}

	/**
	 * JavaBeans boilerplate code...
	 *
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * JavaBeans boilerplate code...
	 *
	 * @param propertyName
	 * @param listener
	 */
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * JavaBeans boilerplate code...
	 *
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * JavaBeans boilerplate code...
	 *
	 * @param propertyName
	 * @param listener
	 */
	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName,
				listener);
	}

	private boolean isEitherValueNotNull(Object oldValue, Object newValue) {
		return oldValue != null || newValue != null;
	}

	private void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		if (isEitherValueNotNull(oldValue, newValue)) {
			propertyChangeSupport.firePropertyChange(propertyName,
					oldValue, newValue);
		}
	}

	protected boolean updating = false;

	protected int oldSelection = 0;
	protected int selection = 0;
	protected String oldRawText = "";
   protected boolean replacedSelectedText = false;

	private VerifyListener verifyListener = new VerifyListener() {
		@Override
		public void verifyText(VerifyEvent e) {
         // If the edit mask is already full, don't let the user type
         // any new characters
         if (editMaskParser.isComplete() && // should eventually be .isFull() to account for optional characters
               e.start == e.end &&
               e.text.length() > 0)
         {
            e.doit=false;
            return;
         }

			oldSelection = selection;
			Point selectionRange = text.getSelection();
         selection = selectionRange.x;

			if (!updating) {
   			replacedSelectedText = false;
   			if (selectionRange.y - selectionRange.x > 0 && e.text.length() > 0) {
   			   replacedSelectedText = true;
   			}
            // If the machine is loaded down (ie: spyware, malware), we might
            // get another keystroke before asyncExec can process, so we use
            // greedyExec instead.
            SWTUtil.greedyExec(Display.getCurrent(), updateTextField);
//				Display.getCurrent().asyncExec(updateTextField);
         }
		}
	};

	private Runnable updateTextField = new Runnable() {
		@Override
		public void run() {
			updating = true;
			try {
				if (!text.isDisposed()) {
					Boolean oldIsComplete = Boolean.valueOf(editMaskParser.isComplete());

					editMaskParser.setInput(text.getText());
					text.setText(editMaskParser.getFormattedResult());
					String newRawText = editMaskParser.getRawResult();

					updateSelectionPosition(newRawText);
					firePropertyChangeEvents(oldIsComplete, newRawText);
				}
			} finally {
				updating = false;
			}
		}

		private void updateSelectionPosition(String newRawText) {

         // Adjust the selection
         if (isInsertingNewCharacter(newRawText) || replacedSelectedText) {
            // Find the position after where the new character was actually inserted
            int selectionDelta =
               editMaskParser.getNextInputPosition(oldSelection)
               - oldSelection;
            if (selectionDelta == 0) {
               selectionDelta = editMaskParser.getNextInputPosition(selection)
               - selection;
            }
            selection += selectionDelta;
         }

			// Did we just type something that was accepted by the mask?
			if (!newRawText.equals(oldRawText)) { // yep

            // If the user hits <end>, bounce them back to the end of their actual input
				int firstIncompletePosition = editMaskParser.getFirstIncompleteInputPosition();
				if (firstIncompletePosition > 0 && selection > firstIncompletePosition)
					selection = firstIncompletePosition;
				text.setSelection(new Point(selection, selection));

			} else { // nothing was accepted by the mask

				// Either we backspaced over a literal or we typed an illegal character
				if (selection > oldSelection) { // typed an illegal character; backup
					text.setSelection(new Point(selection-1, selection-1));
				} else { // backspaced over a literal; don't interfere with selection position
					text.setSelection(new Point(selection, selection));
				}
			}
			oldRawText = newRawText;
		}

		private boolean isInsertingNewCharacter(String newRawText) {
			return newRawText.length() > oldRawText.length();
		}

		private void firePropertyChangeEvents(Boolean oldIsComplete, String newRawText) {
			Boolean newIsComplete = Boolean.valueOf(editMaskParser.isComplete());
			if (!oldIsComplete.equals(newIsComplete)) {
				firePropertyChange(FIELD_COMPLETE, oldIsComplete, newIsComplete);
			}
			if (!newRawText.equals(oldValidRawText)) {
				if ( newIsComplete.booleanValue() || "".equals(newRawText) || fireChangeOnKeystroke) {
					firePropertyChange(FIELD_RAW_TEXT, oldValidRawText, newRawText);
					firePropertyChange(FIELD_TEXT, oldValidText, text.getText());
					oldValidText = text.getText();
					oldValidRawText = newRawText;
				}
			}
		}
	};

	private FocusListener focusListener = new FocusAdapter() {
		@Override
		public void focusGained(FocusEvent e) {
			selection = editMaskParser.getFirstIncompleteInputPosition();
			text.setSelection(selection, selection);
		}
	};

	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			text.removeVerifyListener(verifyListener);
			text.removeFocusListener(focusListener);
			text.removeDisposeListener(disposeListener);
		}
	};

}
