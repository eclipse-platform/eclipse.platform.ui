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
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;
import org.eclipse.ui.keys.ParseException;

/**
 * A wrapper around the SWT text widget that traps literal key presses and 
 * converts them into key sequences for display.  There are two types of key
 * strokes that are displayed: complete and incomplete.  A complete key stroke
 * is one with a natural key, while an incomplete one has no natural key.  
 * Incomplete key strokes are only displayed until they are made complete or
 * their component key presses are released.
 */
public final class KeySequenceText {

	/** 
	 * The special integer value for the maximum number of strokes indicating
	 * that an infinite number should be allowed.
	 */
	public static final int INFINITE = -1;
	/** An empty string instance for use in clearing text values. */
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/** The text of the key sequence -- containing only the complete key strokes. */
	private KeySequence keySequence = KeySequence.getInstance();
	/** The maximum number of key strokes permitted in the sequence. */
	private int maxStrokes = INFINITE;
	/** The text widget that is wrapped for this class. */
	private final Text text;
	/** The listener that makes sure that the text widget remains up-to-date
	 * with regards to external modification of the text (e.g., cut & pasting).
	 */
	private final UpdateSequenceListener updateSequenceListener = new UpdateSequenceListener();

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

		/* TODO doug: pls. investigate. works until one backspaces to an empty text field, at which point the font gets changed somehow 
		if ("carbon".equals(SWT.getPlatform())) {
			// don't worry about this font name here, it is the official menu font and point size on the mac.
			final Font font = new Font(text.getDisplay(), "Lucida Grande", 13, SWT.NORMAL); //$NON-NLS-1$
		
			text.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					font.dispose();
				}
			});	
			
			text.setFont(font);		
		}
		*/

		// Add the key listener.
		final Listener keyFilter = new KeyTrapListener();
		text.addListener(SWT.KeyUp, keyFilter);
		text.addListener(SWT.KeyDown, keyFilter);
		
		// Add the focus listener that attaches the global traversal filter.
		text.addFocusListener(new TraversalFilterManager());

		// Add an internal modify listener.
		text.addModifyListener(updateSequenceListener);
	}

	/**
	 * Changes the font on the underlying text widget.
	 * @param font The new font.
	 */
	public void setFont(Font font) {
		text.setFont(font);
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
		keySequence = KeySequence.getInstance();
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
	
	/** 
	 * An accessor for the underlying text widget's contents.
	 * @return The text contents of this entry; never <code>null</code>.
	 */
	final String getText() {
		return text.getText();
	}

	/**
	 * Tests whether the current key sequence has a stroke with no natural key.
	 * @return <code>true</code> is there is an incomplete stroke; 
	 * <code>false</code> otherwise.
	 */
	public final boolean hasIncompleteStroke() {
		return !keySequence.isComplete();
	}

	/**
	 * A mutator for the enabled state of the wrapped widget.
	 * @param enabled Whether the text field should be enabled.
	 */
	public final void setEnabled(final boolean enabled) {
		text.setEnabled(enabled);
	}

	/**
	 * <p>
	 * A mutator for the key sequence stored within this widget.  The text and
	 * caret position are updated.
	 * </p>
	 * <p>
	 * All sequences are limited to maxStrokes number of strokes in length.
	 * If there are already that number of strokes, then it does not show
	 * incomplete strokes, and does not keep track of them.
	 * </p>
	 *   
	 * @param newKeySequence The new key sequence for this widget; may be
	 * <code>null</code> if none.
	 */
	public void setKeySequence(KeySequence newKeySequence) {
		keySequence = newKeySequence;
		
		// Trim any extra strokes.
		if (maxStrokes != INFINITE) {
			List keyStrokes = new ArrayList(keySequence.getKeyStrokes());
			int keyStrokesSize = keyStrokes.size();
			for (int i = keyStrokesSize - 1; i >= maxStrokes; i--) {
				keyStrokes.remove(i);
			}
			keySequence = KeySequence.getInstance(keyStrokes);	
		}
				
		// We need to update the text, but we don't need to synchronize.
		text.removeModifyListener(updateSequenceListener);
		text.setText(keySequence.format());
		text.addModifyListener(updateSequenceListener);

		// Update the caret position.
		text.setSelection(getText().length());
	}

	/**
	 * A mutator for the layout information associated with the wrapped widget.
	 * @param layoutData The layout information; must not be <code>null</code>.
	 */
	public void setLayoutData(Object layoutData) {
		text.setLayoutData(layoutData);
	}

	/**
	 * A mutator for the maximum number of strokes that are permitted in this
	 * widget at one time.
	 * @param maximumStrokes The maximum number of strokes; should be a positive
	 * integer or <code>INFINITE</code>.
	 */
	public void setMaxStrokes(int maximumStrokes) {
		if ((maximumStrokes > 0) || (maximumStrokes == INFINITE)) {
			maxStrokes = maximumStrokes;
		}
	}

	/**
	 * A key listener that traps incoming events and displays them in the 
	 * wrapped text field.  It has no effect on traversal operations.
	 */
	private class KeyTrapListener implements Listener {
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
		public void handleEvent(Event event) {
			List keyStrokes = new ArrayList(getKeySequence().getKeyStrokes());
			
			if (event.type == SWT.KeyDown) {
				if ((event.character == SWT.BS) && (event.stateMask == 0)) {
					// Remove the last key stroke.
					if (!keyStrokes.isEmpty()) {
						keyStrokes.remove(keyStrokes.size() - 1);
					}
					
				} else {
					// Handles the key pressed event.
					// Remove the incomplete stroke, if any.
					if ((hasIncompleteStroke()) && (!keyStrokes.isEmpty())) {
						keyStrokes.remove(keyStrokes.size() - 1);
					}
					
					// Add the new stroke.
					int key = KeySupport.convertEventToUnmodifiedAccelerator(event);
					KeyStroke stroke = KeySupport.convertAcceleratorToKeyStroke(key);
					keyStrokes.add(stroke);
				}

			} else if ((event.type == SWT.KeyUp) && (hasIncompleteStroke())) {
				/* Handles the key released event, which is only relevant if
				 * there is an incomplete stroke.
				 */
				/* Figure out the SWT integer representation of the remaining
				 * values.
				 */
				Event mockEvent = new Event();
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
				int key = KeySupport.convertEventToUnmodifiedAccelerator(mockEvent);
				KeyStroke remainingStroke = KeySupport.convertAcceleratorToKeyStroke(key);
				if (!keyStrokes.isEmpty()) {
					keyStrokes.remove(keyStrokes.size() - 1);
				}
				if (!remainingStroke.getModifierKeys().isEmpty()) {
					keyStrokes.add(remainingStroke);
				}

			}
					
			// Update the underlying widget.
			setKeySequence(KeySequence.getInstance(keyStrokes));

			// Prevent the event from reaching the widget.
			event.doit = false;
		}
	}

	/**
	 * A traversal listener that blocks all traversal except for tabs and arrow
	 * keys.
	 */
	private class TraversalFilter implements Listener {
		/**
		 * Handles the traverse event on the text field wrapped by this class.
		 * It swallows all traverse events example for tab and arrow key 
		 * navigation.  The other forms of navigation can be reached by tabbing
		 * off of the control.
		 * 
		 * @param event The trigger event; must not be <code>null</code>.
		 */
		public void handleEvent(Event event) {
			switch (event.detail) {
				case SWT.TRAVERSE_ESCAPE :
				case SWT.TRAVERSE_MNEMONIC :
				case SWT.TRAVERSE_NONE :
				case SWT.TRAVERSE_PAGE_NEXT :
				case SWT.TRAVERSE_PAGE_PREVIOUS :
				case SWT.TRAVERSE_RETURN :
					event.type = SWT.None;
					event.doit = false;
					break;
					
				case SWT.TRAVERSE_TAB_NEXT :
				case SWT.TRAVERSE_TAB_PREVIOUS :				
					// Check if modifiers other than just 'Shift' were down.
					if ((event.stateMask & (SWT.MODIFIER_MASK ^ SWT.SHIFT)) != 0) {
						// Modifiers other than shift were down.
						event.type = SWT.None;
						event.doit = false;
						break;
					}
					// fall through -- either no modifiers, or just shift.
				
				case SWT.TRAVERSE_ARROW_NEXT :
				case SWT.TRAVERSE_ARROW_PREVIOUS :
				default :
					// Let the traversal happen, but clear the incomplete stroke
					if (hasIncompleteStroke()) {
						List keyStrokes = new ArrayList(getKeySequence().getKeyStrokes());
						if (!keyStrokes.isEmpty()) {
							keyStrokes.remove(keyStrokes.size() - 1);
						}
						setKeySequence(KeySequence.getInstance(keyStrokes));
					}
			}
			
		}
	}
	
	/**
	 * The manager resposible for installing and removing the traversal filter
	 * when the key sequence entry widget gains and loses focus.
	 */
	private class TraversalFilterManager implements FocusListener {
		/** The managed filter.  We only need one instance. */
		private TraversalFilter filter = new TraversalFilter();
		
		/**
		 * Attaches the global traversal filter.
		 * @param event Ignored.
		 */
		public void focusGained(FocusEvent event) {
			Display.getCurrent().addFilter(SWT.Traverse, filter);
		}
		
		/**
		 * Detaches the global traversal filter.
		 * @param event Ignored.
		 */
		public void focusLost(FocusEvent event) {
			Display.getCurrent().removeFilter(SWT.Traverse, filter);
		}
	}

	/**
     * A modification listener that makes sure that external events to this 
     * class (i.e., direct modification of the underlying text) do not break
     * this class' view of the world.
     */
	private class UpdateSequenceListener implements ModifyListener {
		/**
		 * Handles the modify event on the underlying text widget.
         * @param event The triggering event; ignored.
         */
		public void modifyText(ModifyEvent event) {
			try {
				// The original sequence.
				KeySequence originalSequence = getKeySequence();

				// The new sequence drawn from the text.
				String contents = getText();
				KeySequence newSequence = KeySequence.getInstance(contents);

				// Check to see if they're the same.
				if (!originalSequence.equals(newSequence)) {
					setKeySequence(newSequence);
				}
				
			} catch (ParseException e) {
				// Abort any cut/paste-driven modifications
				setKeySequence(getKeySequence());
			}
		}
	}
}