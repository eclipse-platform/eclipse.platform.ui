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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.AbstractBreakpointOrganizer;
import org.eclipse.debug.ui.IBreakpointOrganizer;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Breakpoint organizers for breakpoints that don't fall into a category.
 * 
 * @since 3.1
 */
public class OtherBreakpointOrganizer extends AbstractBreakpointOrganizer implements IBreakpointOrganizer{
	
	public static IBreakpointOrganizer fgDefault =new OtherBreakpointOrganizer();
	public static IAdaptable[] fgOtherCategory = new IAdaptable[]{new OtherCategory()};

	public static IBreakpointOrganizer getOrganizer() {
		return fgDefault;
	}
	
	public static IAdaptable[] getCategories() {
		return fgOtherCategory;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#getCategories(org.eclipse.debug.core.model.IBreakpoint)
     */
    public IAdaptable[] getCategories(IBreakpoint breakpoint) {
        return null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IBreakpointOrganizer#getLabel()
	 */
	public String getLabel() {
		return DebugUIViewsMessages.getString("OtherBreakpointOrganizer.0"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IBreakpointOrganizer#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_VIEW_BREAKPOINTS);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IBreakpointOrganizer#getIdentifier()
	 */
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

}

class OtherCategory extends PlatformObject {
}
