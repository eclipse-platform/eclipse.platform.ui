/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.commands.ws;

import java.util.Map;

import org.eclipse.ui.PlatformUI;

/**
 * A handler that displays the key assist dialog when executed.
 * 
 * @since 3.1
 */
public class ShowKeyAssistHandler extends WorkbenchWindowHandlerDelegate {

    /**
     * Opens the key assistant. This should never be called until initialization
     * occurs.
     * 
     * @param parameterValuesByName
     *            Ignored
     * @return <code>null</code>
     */
    public Object execute(Map parameterValuesByName) {
        PlatformUI.getWorkbench().getContextSupport().openKeyAssistDialog();
        return null;
    }
}
