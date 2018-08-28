/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.ui.IWorkbenchWindow;

/**
 * The selection service for a window.
 */
/* package */
class WindowSelectionService extends AbstractSelectionService {

    /**
     * The window.
     */
    private IWorkbenchWindow window;

    /**
     * Creates a new selection service for the given window.
     */
    public WindowSelectionService(IWorkbenchWindow window) {
        setWindow(window);
    }

    /**
     * Sets the window.
     */
    private void setWindow(IWorkbenchWindow window) {
        this.window = window;
    }

    /**
     * Returns the window.
     */
    protected IWorkbenchWindow getWindow() {
        return window;
    }

    /*
     * @see AbstractSelectionService#createPartTracker(String)
     */
    @Override
	protected AbstractPartSelectionTracker createPartTracker(String partId) {
        return new WindowPartSelectionTracker(getWindow(), partId);
    }

}
