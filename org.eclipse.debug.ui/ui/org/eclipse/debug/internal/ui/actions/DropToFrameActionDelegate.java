/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDropToFrame;

/**
 * Action delegate which performs a drop to frame.
 */
public class DropToFrameActionDelegate extends AbstractListenerActionDelegate {

    /**
     * Performs the drop to frame.
     * @see AbstractDebugActionDelegate#doAction(Object)
     */
    protected void doAction(Object element) throws DebugException {
        if (element instanceof IDropToFrame) {
            IDropToFrame dropToFrame= (IDropToFrame) element;
            if (dropToFrame.canDropToFrame()) {
                dropToFrame.dropToFrame();
            }
        }
    }

    /**
     * @see AbstractDebugActionDelegate#isRunInBackground()
     */
    protected boolean isRunInBackground() {
        return true;
    }

    /**
     * Enable the action for implementers of IDropToFrame which are able to perform
     * the drop to frame operation.
     */
    protected boolean isEnabledFor(Object element) {
        return element instanceof IDropToFrame && ((IDropToFrame) element).canDropToFrame();
    }

}
