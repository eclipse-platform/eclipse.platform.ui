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
package org.eclipse.debug.internal.ui.views.breakpoints;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.AbstractBreakpointOrganizer;

/**
 * Breakpoint organizers for breakpoint types.
 * 
 * @since 3.1
 */
public class BreakpointTypeOrganizer extends AbstractBreakpointOrganizer {
	
	private Map fTypes = new HashMap();

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#getCategories(org.eclipse.debug.core.model.IBreakpoint)
     */
    public IAdaptable[] getCategories(IBreakpoint breakpoint) {
    	String name = DebugPlugin.getDefault().getBreakpointManager().getTypeName(breakpoint);
    	if (name != null) {
    		IAdaptable[] category = (IAdaptable[]) fTypes.get(name);
    		if (category == null) {
    			category = new IAdaptable[]{new BreakpointTypeCategory(name)};
    			fTypes.put(name, category);
    		}
    		return category;
    	}
    	return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#dispose()
     */
    public void dispose() {
    	fTypes.clear();
    }

}
