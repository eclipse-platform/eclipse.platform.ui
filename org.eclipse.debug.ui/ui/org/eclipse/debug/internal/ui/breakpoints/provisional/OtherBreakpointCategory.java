/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.breakpoints.provisional;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Category for breakpoints in "other" categories.   Clients which provide 
 * custom content in the Breakpoints view may instantiate this object to 
 * represent elements in a breakpoint organizer that do not fall into any known
 * category.
 * 
 * @since 3.6
 * 
 * @see IBreakpointContainer
 * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate
 */
public class OtherBreakpointCategory extends PlatformObject implements IWorkbenchAdapter {
    
    private static Map fOthers = new HashMap();
    private IBreakpointOrganizer fOrganizer;
    
    
    public static IAdaptable[] getCategories(IBreakpointOrganizer organizer) {
        IAdaptable[] others = (IAdaptable[]) fOthers.get(organizer);
        if (others == null) {
            others = new IAdaptable[]{new OtherBreakpointCategory(organizer)};
            fOthers.put(organizer, others);
        }
        return others;
    }
    
    /**
     * Constructs an 'other' category for the given organizer.
     * 
     * @param organizer breakpoint organizer
     */
    private OtherBreakpointCategory(IBreakpointOrganizer organizer) {
        fOrganizer = organizer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_VIEW_BREAKPOINTS);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
        return fOrganizer.getOthersLabel();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        return null;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof OtherBreakpointCategory) {
            OtherBreakpointCategory category = (OtherBreakpointCategory) obj;
            return fOrganizer.equals(category.fOrganizer);
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return fOrganizer.hashCode();
    }    
}
