/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.actions.RemoveAllTerminatedAction;

/**
 * Remove all terminated action delegate for the console. Computes the elements as
 * all registered launches rather than those in the debug view.
 */
public class ConsoleRemoveAllTerminatedActionDelegate extends RemoveAllTerminatedAction {

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.RemoveAllTerminatedAction#getElements()
     */
    public Object[] getElements() {
        return DebugPlugin.getDefault().getLaunchManager().getLaunches();
    }
}
