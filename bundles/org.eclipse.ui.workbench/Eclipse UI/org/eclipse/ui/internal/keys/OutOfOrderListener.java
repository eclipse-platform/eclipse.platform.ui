/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.keys;

import java.util.List;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * A listener that makes sure that global key bindings are processed if no other
 * listeners do any useful work.
 * 
 * @since 3.0
 */
public class OutOfOrderListener implements Listener {
    /**
     * The time at which this listener was last registered to an event. This is
     * the <code>event.time</code> value.
     * 
     * @since 3.1
     */
    private int active = Integer.MIN_VALUE;

    /**
     * The keyboard interface to which the event should be passed if it is not
     * eaten.
     */
    private final WorkbenchKeyboard keyboard;

    /**
     * Constructs a new instance of <code>OutOfOrderListener</code> with a
     * reference to the keyboard interface which should be allowed to process
     * uneaten events.
     * 
     * @param workbenchKeyboard
     *            The keyboard interface for the workbench capable of processing
     *            key bindings; must not be <code>null</code>.
     */
    public OutOfOrderListener(WorkbenchKeyboard workbenchKeyboard) {
        keyboard = workbenchKeyboard;
    }

    /**
     * Handles the key down event on a widget by passing uneaten events to the
     * key binding architecture. This is used to allow special keys to reach the
     * widget first -- before being processed by the key binding architecture.
     * 
     * @param event
     *            The event to process; must not be <code>null</code>
     */
    public void handleEvent(Event event) {
        // Always remove myself as a listener.
        final Widget widget = event.widget;
        if ((widget != null) && (!widget.isDisposed())) {
            widget.removeListener(event.type, this);
        }

        /*
         * If the event is still up for grabs, then re-route through the global
         * key filter.
         */
        if (event.doit) {
            List keyStrokes = WorkbenchKeyboard
                    .generatePossibleKeyStrokes(event);
            keyboard.processKeyEvent(keyStrokes, event);
        }
    }

    /**
     * Returns whether this listener has been hooked by this event already.
     * 
     * @param timeRegistered
     *            The <code>event.time</code> for the current event.
     * @return <code>true</code> if this listener is registered for a
     *         different event; <code>false</code> otherwise.
     * 
     * @since 3.1
     */
    final boolean isActive(final int timeRegistered) {
        return (active == timeRegistered);
    }

    /**
     * Sets the event time at which this listener was last registered with a
     * widget.
     * 
     * @param timeRegistered
     *            The time at which this listener was last registered with a
     *            widget.
     * 
     * @since 3.1
     */
    final void setActive(final int timeRegistered) {
        active = timeRegistered;
    }
}
