/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.keys;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchWindow;

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
     * The workbench window associated with this state. The state can only
     * exist for one window. When the focus leaves this window then the mode
     * must automatically be reset.
     */
    private IWorkbenchWindow associatedWindow;

    /**
     * This is the current extent of the sequence entered by the user. In an
     * application with only single-stroke key bindings, this will also be
     * empty. However, in applications with multi-stroke key bindings, this is
     * the sequence entered by the user that partially matches another one of
     * the application's active key bindings.
     */
    private KeySequence currentSequence;

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
        workbench = workbenchToNotify;
        associatedWindow = workbench.getActiveWorkbenchWindow();
    }

    /**
     * An accessor for the workbench window associated with this state. This
     * should never be <code>null</code>, as the setting follows the last
     * workbench window to have focus.
     * 
     * @return The workbench window to which the key binding architecture is
     *         currently attached; should never be <code>null</code>.
     */
    IWorkbenchWindow getAssociatedWindow() {
        return associatedWindow;
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
     * Gets the status line contribution item which the key binding
     * architecture uses to keep the user up-to-date as to the current state.
     * 
     * @return The status line contribution item, if any; <code>null</code>,
     *         if none.
     */
    StatusLineContributionItem getStatusLine() {
        if (associatedWindow instanceof WorkbenchWindow) {
            WorkbenchWindow window = (WorkbenchWindow) associatedWindow;
            IStatusLineManager statusLine = window.getStatusLineManager();
            // TODO implicit dependency on IDE's action builder
            // @issue implicit dependency on IDE's action builder
            if (statusLine != null) { // this can be null if we're exiting
                IContributionItem item = statusLine
                        .find("ModeContributionItem"); //$NON-NLS-1$
                if (item instanceof StatusLineContributionItem) {
                    return ((StatusLineContributionItem) item);
                }
            }
        }

        return null;
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
        currentSequence = KeySequence.getInstance();
        updateStatusLines();
    }

    /**
     * A mutator for the workbench window to which this state is associated.
     * 
     * @param window
     *            The workbench window to associated; should never be <code>null</code>.
     */
    void setAssociatedWindow(IWorkbenchWindow window) {
        associatedWindow = window;
    }

    /**
     * A mutator for the partial sequence entered by the user.
     * 
     * @param sequence
     *            The current key sequence; should not be <code>null</code>,
     *            but may be empty.
     */
    void setCurrentSequence(KeySequence sequence) {
        currentSequence = sequence;
        updateStatusLines();
    }

    /**
     * Updates the text of the status line of the associated shell with the
     * current sequence.
     */
    private void updateStatusLines() {
        StatusLineContributionItem statusLine = getStatusLine();
        if (statusLine != null) {
            statusLine.setText(getCurrentSequence().format());
        }
    }
}
