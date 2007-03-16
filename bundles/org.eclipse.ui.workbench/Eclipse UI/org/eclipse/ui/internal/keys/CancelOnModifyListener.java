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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * A listener that removes the out-of-order listener if a modification occurs
 * before reaching it. This is a workaround for Bug 53497.
 * 
 * @since 3.0
 */
final class CancelOnModifyListener implements Listener {

    /**
     * The listener to remove when this listener catches any event. This value
     * should not be <code>null</code>.
     */
    private final Listener chainedListener;

    /**
     * Constructs a new instance of <code>CancelOnModifyListener</code>
     * 
     * @param listener
     *            The listener which should be removed in the event of a
     *            modification event arriving; should not be <code>null</code>.
     */
    CancelOnModifyListener(Listener listener) {
        chainedListener = listener;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
        Widget widget = event.widget;
        widget.removeListener(SWT.Modify, this);
        widget.removeListener(SWT.KeyDown, chainedListener);
    }
}
