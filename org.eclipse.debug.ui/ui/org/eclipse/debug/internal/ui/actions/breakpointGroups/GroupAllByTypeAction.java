/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Action which groups all breakpoints based on their breakpoint type
 */
public class GroupAllByTypeAction extends AbstractGroupBreakpointsAction {

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.breakpointGroups.AbstractGroupBreakpointsAction#getGroup(org.eclipse.debug.core.model.IBreakpoint)
     */
    protected String getGroup(IBreakpoint breakpoint) {
        return DebugPlugin.getDefault().getBreakpointManager().getTypeName(breakpoint);
    }

}
