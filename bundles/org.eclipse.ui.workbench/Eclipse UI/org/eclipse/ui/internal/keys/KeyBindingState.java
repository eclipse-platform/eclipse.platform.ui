/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.keys;

import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IStatusLineManager;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;

import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.util.StatusLineContributionItem;

/**
 * <p>
 * The mutable state of the key binding architecture. This is the only piece of
 * the key binding architecture that changes (internally). It keeps track of
 * what partial key strokes the user has entered. In the case of functional
 * groups of key bindings, it allows the user to keep part of the key sequence
 * even after a match has been made. Only after releasing all of the modifier
 * keys would the sequence reset itself.
 * </p>
 * <p>
 * In the current implementation, a partial reset results in only one key
 * stroke being left in the sequence. However, this may change in the future.
 * </p>
 * 
 * @since 3.0
 */
class KeyBindingState {

	/**
	 * A utility method for checking whether the first key stroke in a list
	 * contains any modifier keys.
	 * 
	 * @param keyStrokes
	 *            The list of key strokes to check; must not be <code>null</code>.
	 * @return <code>true</code> if the list is not empty and the first key
	 *         stroke has modifier keys; <code>false</code> otherwise.
	 */
	private static boolean isFirstStrokeModified(List keyStrokes) {
		if (keyStrokes.isEmpty()) {
			return false;
		}

		KeyStroke firstStroke = (KeyStroke) keyStrokes.get(0);
		return (!firstStroke.getModifierKeys().isEmpty());

	}
	/**
	 * Whether the key sequence should be completely cleared when this state is
	 * told to reset itself. Otherwise, the key sequence will only reset part
	 * of the key sequence.
	 */
	private boolean collapseFully;
	/**
	 * This is the current extent of the sequence entered by the user. In an
	 * application with only single-stroke key bindings, this will also be
	 * empty. However, in applications with multi-stroke key bindings, this is
	 * the sequence entered by the user that partially matches another one of
	 * the application's active key bindings.
	 */
	private KeySequence currentSequence;
	/**
	 * whether the state can be reset safely -- without destroying a partial
	 * sequence the user has entered. This is used to make the transition from
	 * not fully collapsable to fully collapsable.
	 */
	private boolean safeToReset;
	/**
	 * The workbench that should be notified of changes to the key binding
	 * state. This is done by updating one of the contribution items on the
	 * status line.
	 */
	private final IWorkbench workbench;

	/**
	 * Constructs a new instance of <code>KeyBindingState</code> with an
	 * empty key sequence, set to reset fully.
	 * 
	 * @param workbenchToNotify
	 *            The workbench that this state should keep advised of changes
	 *            to the key binding state; must not be <code>null</code>.
	 */
	KeyBindingState(IWorkbench workbenchToNotify) {
		currentSequence = KeySequence.getInstance();
		collapseFully = true;
		safeToReset = true;
		workbench = workbenchToNotify;
	}

	/**
	 * An accessor for the current key sequence waiting for completion.
	 * 
	 * @return The current incomplete key sequence; never <code>null</code>,
	 *         but may be empty.
	 */
	KeySequence getCurrentSequence() {
		return currentSequence;
	}

	/**
	 * Whether it is safe for someone to issue a reset after switching to a
	 * fully collapsable state. This checks to see if they have been any
	 * changes to the sequence made since the last reset.
	 * 
	 * @return <code>true</code> if the state can be reset when the state
	 *         changes to fully collapsable; <code>false</code> otherwise.
	 */
	boolean isSafeToReset() {
		return safeToReset;
	}

	/**
	 * <p>
	 * Resets the state based on the current properties. If the state is to
	 * collapse fully or if there are no key strokes, then it sets the state to
	 * have an empty key sequence. Otherwise, it leaves the first key stroke in
	 * the sequence.
	 * </p>
	 * <p>
	 * The workbench's status lines are updated, if appropriate.
	 * </p>
	 */
	void reset() {
		if (collapseFully) {
			safeToReset = true;
			currentSequence = KeySequence.getInstance();
			updateStatusLines();
		} else {
			List currentStrokes = currentSequence.getKeyStrokes();
			if (!currentStrokes.isEmpty()) {
				safeToReset = true;
				KeyStroke firstStroke = (KeyStroke) currentStrokes.get(0);
				if (firstStroke.getModifierKeys().isEmpty()) {
					currentSequence = KeySequence.getInstance();
				} else {
					currentSequence = KeySequence.getInstance(firstStroke);
				}
				updateStatusLines();
			}
		}
	}

	/**
	 * A mutator for whether the state should collapse the state of the mode
	 * completely when next asked (i.e., remove all key strokes).
	 * 
	 * @param collapse
	 *            Whether the state should collapse fully when reset.
	 */
	void setCollapseFully(boolean collapse) {
		collapseFully = collapse;
	}

	/**
	 * A mutator for the partial sequence entered by the user.
	 * 
	 * @param sequence
	 *            The current key sequence; should not be <code>null</code>,
	 *            but may be empty.
	 */
	void setCurrentSequence(KeySequence sequence) {
		List keyStrokes = sequence.getKeyStrokes();
		if ((keyStrokes.size() > 2) && (isFirstStrokeModified(keyStrokes))) {
			safeToReset = false;
		}
		currentSequence = sequence;
		updateStatusLines();
	}

	/**
	 * Updates the text of the given window's status line with the given text.
	 * 
	 * @param window
	 *            the window
	 * @param text
	 *            the text
	 */
	private void updateStatusLine(IWorkbenchWindow window, String text) {
		if (window instanceof WorkbenchWindow) {
			IStatusLineManager statusLine = ((WorkbenchWindow) window).getStatusLineManager();
			// TODO implicit dependency on IDE's action builder
			// @issue implicit dependency on IDE's action builder
			IContributionItem item = statusLine.find("ModeContributionItem"); //$NON-NLS-1$
			if (item instanceof StatusLineContributionItem) {
				((StatusLineContributionItem) item).setText(text);
			}
		}
	}

	/**
	 * Updates the text of the status lines with the current mode.
	 */
	private void updateStatusLines() {
		// Format the mode into text.
		String text = getCurrentSequence().format();
		// Update each open window's status line.
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			updateStatusLine(windows[i], text);
		}
	}
}
