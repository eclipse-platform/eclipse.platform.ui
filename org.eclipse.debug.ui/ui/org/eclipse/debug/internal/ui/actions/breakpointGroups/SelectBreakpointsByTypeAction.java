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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * 
 */
public class SelectBreakpointsByTypeAction extends AbstractSelectBreakpointsAction {

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.breakpointGroups.AbstractSelectBreakpointsAction#breakpointsMatch(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.debug.core.model.IBreakpoint)
     */
    public boolean breakpointsMatch(IBreakpoint breakpointOne, IBreakpoint breakpointTwo) {
        IMarker markerOne = breakpointOne.getMarker();
        IMarker markerTwo = breakpointTwo.getMarker();
        if (markerOne != null && markerTwo != null) {
            try {
                return markerOne.getType().equals(markerTwo.getType()); 
            } catch (CoreException e1) {
            }
        }
        return false;
    }



}
