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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Widget;

/**
 * A listener that makes sure that out-of-order processing occurs if no other
 * verify listeners do any work.
 * 
 * @since 3.0
 */
class OutOfOrderVerifyListener implements VerifyKeyListener {

    /**
     * The time at which this listener was last registered to an event. This is
     * the <code>event.time</code> value.
     * 
     * @since 3.1
     */
    private int active = Integer.MIN_VALUE;

    /**
     * The listener that will be chained in if the verify event has not been
     * eaten yet.
     */
    private OutOfOrderListener chainedListener;

    /**
     * Constructs a new instance of <code>OutOfOrderVerifyListener</code> with
     * the listener that will be chained in.
     * 
     * @param outOfOrderListener
     *            The listener that should be attached to the widget if the
     *            verify event is not eaten; must not be <code>null</code>.
     */
    OutOfOrderVerifyListener(OutOfOrderListener outOfOrderListener) {
        chainedListener = outOfOrderListener;
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

    /**
     * Checks whether any other verify listeners have triggered. If not, then it
     * sets up the top-level out-of-order listener.
     * 
     * @param event
     *            The verify event after it has been processed by all other
     *            verify listeners; must not be <code>null</code>.
     */
    public void verifyKey(VerifyEvent event) {
        // Always remove the listener.
        final Widget widget = event.widget;
        if ((widget instanceof StyledText) && (!widget.isDisposed())) {
            ((StyledText) widget).removeVerifyKeyListener(this);
        }

        /*
         * If the event is still up for grabs, then re-route through the global
         * key filter.
         */
        if (event.doit) {
            widget.addListener(SWT.Modify, new CancelOnModifyListener(
                    chainedListener));
            widget.addListener(SWT.KeyDown, chainedListener);
        }
    }
}
