/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import org.eclipse.ui.IActionBars;

public interface IBrowserViewerContainer {
    /**
     * Closes the container from the inside.
     * @return true if the container closed successfully
     */
    boolean close();
    /**
     * Returns the action bars of the container.
     * @return action bars of the container or <code>null</code> if
     * not available.
     */
    IActionBars getActionBars();
    /**
     * Opens the url in the external browser if
     * internal browser failed to create.
     * @param url
     */
    void openInExternalBrowser(String url);
}