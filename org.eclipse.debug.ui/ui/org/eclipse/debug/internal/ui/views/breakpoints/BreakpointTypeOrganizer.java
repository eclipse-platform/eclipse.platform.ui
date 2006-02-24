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
package org.eclipse.debug.internal.ui.views.breakpoints;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.AbstractBreakpointOrganizerDelegate;
import org.eclipse.debug.ui.BreakpointTypeCategory;
import org.eclipse.debug.ui.IBreakpointTypeCategory;

/**
 * Breakpoint organizers for breakpoint types.
 * 
 * @since 3.1
 */
public class BreakpointTypeOrganizer extends AbstractBreakpointOrganizerDelegate {
	
	private Map fTypes = new HashMap();

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#getCategories(org.eclipse.debug.core.model.IBreakpoint)
     */
    public IAdaptable[] getCategories(IBreakpoint breakpoint) {
        IBreakpointTypeCategory category = (IBreakpointTypeCategory) breakpoint.getAdapter(IBreakpointTypeCategory.class);
        if (category != null) {
            return new IAdaptable[]{category};
        }
    	String name = DebugPlugin.getDefault().getBreakpointManager().getTypeName(breakpoint);
    	if (name != null) {
    		IAdaptable[] categories = (IAdaptable[]) fTypes.get(name);
    		if (categories == null) {
    			categories = new IAdaptable[]{new BreakpointTypeCategory(name)};
    			fTypes.put(name, categories);
    		}
    		return categories;
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
