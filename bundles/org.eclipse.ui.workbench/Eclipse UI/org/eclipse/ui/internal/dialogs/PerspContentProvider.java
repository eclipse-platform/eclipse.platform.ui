/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IObjectActivityManager;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;

public class PerspContentProvider implements IStructuredContentProvider {
    /**
     * PerspContentProvider constructor comment.
     */
    public PerspContentProvider() {
    	super();
    }
    public void dispose() {
    }
    public Object[] getElements(Object element) {
    	if (element instanceof IPerspectiveRegistry) {
    		IPerspectiveRegistry reg = (IPerspectiveRegistry)element;
    		return filteredPerspectives(reg);
    	}
    	return null;
    }
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
    public boolean isDeleted(Object element) {
    	return false;
    }
    
    /**
     * Return the list of perspective descriptors in the supplied registry
     * filtered for roles if appropriate. 
     * 
     * @param registry the registry to use as the source.
     * @return IPerspectiveDescriptor[] the active descriptors.
     */
    IPerspectiveDescriptor[] filteredPerspectives(IPerspectiveRegistry registry) {
        IObjectActivityManager manager = PlatformUI.getWorkbench().getObjectActivityManager(IWorkbenchConstants.PL_PERSPECTIVES, false);
        IPerspectiveDescriptor[] descriptors = registry.getPerspectives();
        if (manager == null) {
            return descriptors;
        }
        Collection activePerspectives = manager.getActiveObjects();
            
        Collection filtered = new ArrayList();
        for (int i = 0; i < descriptors.length; i++) {
            IPerspectiveDescriptor descriptor = descriptors[i];
            // only filter on registry perspectives.  Custom perspectives should never be filtered.
            if (((PerspectiveDescriptor)descriptor).isPredefined()) {
                if (activePerspectives.contains(descriptor.getId())) {
                    filtered.add(descriptor);
                }                
            }
            else {
                filtered.add(descriptor);
            }
        }
            
        return (IPerspectiveDescriptor []) filtered.toArray(new IPerspectiveDescriptor [filtered.size()]);        
    }
}
