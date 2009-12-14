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
package org.eclipse.debug.internal.ui.views.breakpoints;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;

/**
 * Represents a breakpoint category for a specific working set.
 */
public class WorkingSetCategory extends PlatformObject implements IWorkbenchAdapter, IWorkbenchAdapter2 {
    
    private IWorkingSet fWorkingSet;

    /**
     * Constructs a new workings set category for the given working set.
     * 
     * @param workingSet
     */
    public WorkingSetCategory(IWorkingSet workingSet) {
        fWorkingSet = workingSet;
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
        return fWorkingSet.getImageDescriptor();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
        StringBuffer name = new StringBuffer(fWorkingSet.getName());
        if (isDefault()) {
            name.append(DebugUIViewsMessages.WorkingSetCategory_0); 
        }
        return name.toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        return null;
    }
    
    /**
     * Returns the working set for this category.
     * 
     * @return
     */
    public IWorkingSet getWorkingSet() {
        return fWorkingSet;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof WorkingSetCategory) {
            WorkingSetCategory category = (WorkingSetCategory) obj;
            return category.getWorkingSet().equals(fWorkingSet);
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return fWorkingSet.hashCode();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter2#getForeground(java.lang.Object)
     */
    public RGB getForeground(Object element) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter2#getBackground(java.lang.Object)
     */
    public RGB getBackground(Object element) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter2#getFont(java.lang.Object)
     */
    public FontData getFont(Object element) {
        if (isDefault()) {
            FontData[] fontData = JFaceResources.getDefaultFont().getFontData();
            if (fontData != null && fontData.length > 0) {
                FontData data = fontData[0];
                data.setStyle(SWT.BOLD);
                return data;
            }
        }
        return null;
    }
    
    /**
     * Whether this is the default breakpoint working set.
     * 
     * @return whether this is the default breakpoint working set
     */
    private boolean isDefault() {
        return fWorkingSet.equals(BreakpointSetOrganizer.getDefaultWorkingSet());
    }
    
    public String toString() {
        return fWorkingSet.getName();
    }
}

