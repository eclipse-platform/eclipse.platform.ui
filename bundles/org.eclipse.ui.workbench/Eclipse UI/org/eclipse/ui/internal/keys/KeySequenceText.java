/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.keys;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.keys.CharacterKey;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;
import org.eclipse.ui.keys.NaturalKey;

/**
 * A wrapper around the SWT text widget that traps literal key presses and 
 * converts them into key sequences for display.  There are two types of key
 * strokes that are displayed: complete and incomplete.  A complete key stroke
 * is one with a natural key, while an incomplete one has no natural key.  
 * Incomplete key strokes are only displayed until they are made complete or
 * their component key presses are released.
 */
public final class KeySequenceText {
                
	/** An empty string instance for use in clearing text values. */
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/** The text of the key sequence -- containing only the complete key 
	 * strokes.
	 */
	private KeySequence keySequence = null;
	/** The incomplete key stroke, if any. */
	private KeyStroke temporaryStroke = null;
	/** The text widget that is wrapped for this class. */
	private final Text text;

	/**
	 * Constructs an instance of <code>KeySequenceTextField</code> with the 
	 * widget that will be containing the text field.  The font is set based on
	 * this container.
	 * 
	 * @param composite The container widget; must not be <code>null</code>.
	 */
    public KeySequenceText(final Composite composite) {
        // Set up the text field.
		text = new Text(composite, SWT.BORDER);
        text.setFont(composite.getFont());
        
        // Add the key listener.
        final Listener keyFilter = new KeyTrapListener();
        text.addListener(SWT.KeyUp, keyFilter);
        text.addListener(SWT.KeyDown, keyFilter);

        // Add the traversal listener.
        text.addTraverseListener(new FocusTrapListener());
	}

	/**
	 * Adds a listener for modification of the text component.
	 * @param modifyListener The listener that is to be added; must not be
	 * <code>null</code>.
	 */
	public final void addModifyListener(final ModifyListener modifyListener) {
		text.addModifyListener(modifyListener);
	}

	/**
	 * Clears the text field and resets all the internal values.
	 */
	public final void clear() {
		keySequence = null;
		temporaryStroke = null;
		text.setText(EMPTY_STRING);
	}

	/**
	 * An accessor for the <code>KeySequence</code> that corresponds to the 
	 * current state of the text field.  This includes incomplete strokes.
	 * @return The key sequence representation; never <code>null</code>.
	 */
	public final KeySequence getKeySequence() {
		return keySequence;
	}
    
    public final boolean hasIncompleteStroke() {
        return (temporaryStroke != null);
    }

	/**
	 * Checks whether the given key stroke is a temporary key stroke or not.
	 * @param keyStroke The key stroke to check for completion; may be
	 * <code>null</code>, which results in <code>false</code>.
	 * @return <code>true</code> if the key stroke has no natural key; 
	 * <code>false</code> otherwise.
	 */
	private static final boolean isComplete(final KeyStroke keyStroke) {
		if (keyStroke != null) {
			final NaturalKey naturalKey = keyStroke.getNaturalKey();
			
			if (naturalKey instanceof CharacterKey) {
				final CharacterKey characterKey = (CharacterKey) naturalKey;
				return (characterKey.getCharacter() != '\0');
			} else
				return true;
		}

		return false;
	}

	/**
	 * A mutator for the enabled state of the wrapped widget.
	 * @param enabled Whether the text field should be enabled.
	 */
	public final void setEnabled(final boolean enabled) {
		text.setEnabled(enabled);
	}

	/**
	 * A mutator for the layout information associated with the wrapped widget.
	 * @param layoutData The layout information; must not be <code>null</code>.
	 */
	public final void setLayoutData(final Object layoutData) {
		text.setLayoutData(layoutData);
	}

	/**
	 * A mutator for the key sequence and incomplete stroke stored within this
	 * widget.  This does some checking to see if the incomplete stroke is 
	 * really incomplete; if it is complete, then it is rolled into the key 
	 * sequence.  The text and caret position are updated.
	 *   
	 * @param newKeySequence The new key sequence for this widget; may be
	 * <code>null</code> if none.
	 * @param incompleteStroke The new incomplete stroke for this widget; may be
	 * <code>null</code> or incomplete -- both conditions are dealt with.
	 */
	public final void setKeySequence(final KeySequence newKeySequence, final KeyStroke incompleteStroke) {
		// Figure out whether the stroke should be rolled in.
		if (isComplete(incompleteStroke)) {
			if (newKeySequence == null) {
				keySequence = KeySequence.getInstance(incompleteStroke);
			} else {
				final List keyStrokes = new ArrayList(newKeySequence.getKeyStrokes());
				keyStrokes.add(incompleteStroke);
				keySequence = KeySequence.getInstance(keyStrokes);
			}
			temporaryStroke = null;
		} else {
			keySequence = newKeySequence;
			temporaryStroke = incompleteStroke;
		}

		/* Create a dummy (and rather invalid) sequence to get localized display
         * formatting
		 */
        final KeySequence dummySequence;
        
        if (keySequence == null) {
            if (temporaryStroke == null) {
                dummySequence = KeySequence.getInstance();
            } else {
                dummySequence = KeySequence.getInstance(temporaryStroke);
            }
        } else {
            final List keyStrokes = new ArrayList(keySequence.getKeyStrokes());
            if (temporaryStroke != null) {
                keyStrokes.add(temporaryStroke);
            }
            dummySequence = KeySequence.getInstance(keyStrokes);
        }
		
		// TODO doug, why doesn't the following work? 
		// text.setText("carbon".equals(SWT.getPlatform()) ? KeySupport.formatCarbon(dummySequence) : dummySequence.format());
		text.setText(dummySequence.format());
        
        // Update the caret position.
		text.setSelection(text.getText().length());
	}

	/**
	 * A traversal listener that blocks all traversal except for tabs and arrow
	 * keys.
	 */
	private final class FocusTrapListener implements TraverseListener {

		/**
		 * Handles the traverse event on the text field wrapped by this class.
		 * It swallows all traverse events example for tab and arrow key 
		 * navigation.  The other forms of navigation can be reached by tabbing
		 * off of the control.
		 * 
		 * @param event The trigger event; must not be <code>null</code>.
		 */
		public final void keyTraversed(final TraverseEvent event) {
			switch (event.detail) {
				case SWT.TRAVERSE_ESCAPE :
				case SWT.TRAVERSE_MNEMONIC :
				case SWT.TRAVERSE_NONE :
				case SWT.TRAVERSE_PAGE_NEXT :
				case SWT.TRAVERSE_PAGE_PREVIOUS :
				case SWT.TRAVERSE_RETURN :
					event.doit = false;
					break;

				case SWT.TRAVERSE_ARROW_NEXT :
				case SWT.TRAVERSE_ARROW_PREVIOUS :
				case SWT.TRAVERSE_TAB_NEXT :
				case SWT.TRAVERSE_TAB_PREVIOUS :
				default :
					setKeySequence(keySequence, null);
					// Let these ones through.
			}
		}
	}

	/**
	 * A key listener that traps incoming events and displays them in the 
	 * wrapped text field.  It has no effect on traversal operations.
	 */
	private final class KeyTrapListener implements Listener {
		/**
		 * Handles the key pressed and released events on the wrapped text 
		 * widget.  This makes sure to either add the pressed key to the 
		 * temporary key stroke, or complete the current temporary key stroke 
		 * and prompt for the next.  In the case of a key release, this makes 
		 * sure that the temporary stroke is correctly displayed -- 
		 * corresponding with modifier keys that may have been released.
		 * 
		 * @param event The triggering event; must not be <code>null</code>. 
		 */
		public final void handleEvent(final Event event) {
			if (event.type == SWT.KeyDown) {
				// Handles the key pressed event.
				final int key = KeySupport.convertEventToAccelerator(event);
				final KeyStroke stroke = KeySupport.convertAcceleratorToKeyStroke(key);
				setKeySequence(getKeySequence(), stroke);

			} else if (hasIncompleteStroke()) {
				/* Handles the key released event, which is only relevant if
				 * there is an incomplete stroke.
				 */
				/* Figure out the SWT integer representation of the remaining
				 * values.
				 */
				final Event mockEvent = new Event();
				if ((event.keyCode & SWT.MODIFIER_MASK) != 0) {
					// This key up is a modifier key being released.
					mockEvent.stateMask = event.stateMask - event.keyCode;
				} else {
					/* This key up is the other end of a key down that was
					 * trapped by the operating system.
					 */
					mockEvent.stateMask = event.stateMask;
				}

				/* Get a reasonable facsimile of the stroke that is still
				 * pressed.
				 */
				final int key = KeySupport.convertEventToAccelerator(mockEvent);
				final KeyStroke remainingStroke = KeySupport.convertAcceleratorToKeyStroke(key);

				if (remainingStroke.getModifierKeys().isEmpty()) {
					setKeySequence(getKeySequence(), null);
				} else {
					setKeySequence(getKeySequence(), remainingStroke);
				}

			}

			// Prevent the event from reaching the widget.
			event.doit = false;
		}
	}
}