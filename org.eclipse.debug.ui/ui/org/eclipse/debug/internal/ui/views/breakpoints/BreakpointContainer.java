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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * A container of breakpoints, based on a category.
 */
public class BreakpointContainer extends PlatformObject    {

    private IAdaptable fCategory;
    private IBreakpointOrganizer fOrganizer;
    private List fBreakpoints;
    private Map fCategoriesToContainers;
    private IBreakpointOrganizer[] fNesting;
    
    /**
     * Constructs a container of breakpoints for the given category,
     * created by the given organizer.
     * 
     * @param category breakpoint category
     * @param organizer breakpoint organizer
     * @param nesting nested organizers or <code>null</code> if none
     */
    public BreakpointContainer(IAdaptable category, IBreakpointOrganizer organizer, IBreakpointOrganizer[] nesting) {
        fCategory = category;
        fOrganizer = organizer;
        fBreakpoints = new ArrayList();
        fNesting = nesting;
        fCategoriesToContainers = new HashMap();
    }
    
    /**
     * Adds a breakpoint to this container and its nested containers.
     * 
     * @param breakpoint breakpoint to add  
     */
    public void addBreakpoint(IBreakpoint breakpoint) {
        fBreakpoints.add(breakpoint);
        if (fNesting != null && fNesting.length > 0) {
            IBreakpointOrganizer organizer = fNesting[0];
            IAdaptable[] categories = organizer.getCategories(breakpoint);
            if (categories== null || categories.length == 0) {
            	categories = OtherBreakpointOrganizer.getCategories();
            	organizer = OtherBreakpointOrganizer.getOrganizer();
            }
            for (int i = 0; i < categories.length; i++) {
                IAdaptable category = categories[i];
                BreakpointContainer container = (BreakpointContainer) fCategoriesToContainers.get(category);
                if (container == null) {
                    IBreakpointOrganizer[] nesting = null;
                    if (fNesting.length > 1) {
                        nesting = new IBreakpointOrganizer[fNesting.length - 1];
                        System.arraycopy(fNesting, 1, nesting, 0, nesting.length);
                    }
                    container = new BreakpointContainer(category, organizer, nesting);
                    fCategoriesToContainers.put(category, container);
                }
                container.addBreakpoint(breakpoint);
            }
        }
    }
    
    /**
     * Returns the breakpoints in this container
     * 
     * @return the breakpoints in this container
     */
    public IBreakpoint[] getBreakpoints() {
        return (IBreakpoint[]) fBreakpoints.toArray(new IBreakpoint[fBreakpoints.size()]);
    }
    
    /**
     * Returns this container's category.
     * 
     * @return container category
     */
    public IAdaptable getCategory() {
        return fCategory;
    }
    
    /**
     * Returns children as breakpoints or nested containers.
     * 
     * @return children as breakpoints or nested containers
     */
    public Object[] getChildren() {
        if (fCategoriesToContainers.isEmpty()) {
            return getBreakpoints();
        }
        return getContainers(); 
    }
    
    /**
     * Returns the containers nested in this container, possibly empty.
     * 
     * @return the containers nested in this container, possibly empty
     */
    public BreakpointContainer[] getContainers() {
        Collection collection = fCategoriesToContainers.values();
        return (BreakpointContainer[]) collection.toArray(new BreakpointContainer[collection.size()]);
    }
    
    /**
     * Returns this container's organizer.
     * 
     * @return this container's organizer
     */
    public IBreakpointOrganizer getOrganizer() {
        return fOrganizer;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof BreakpointContainer) {
            BreakpointContainer container = (BreakpointContainer) obj;
            return getCategory().equals(container.getCategory());
        }
        return super.equals(obj);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return getCategory().hashCode();
    }
}
