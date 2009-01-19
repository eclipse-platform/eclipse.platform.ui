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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.AbstractBreakpointOrganizerDelegate;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

/**
 * Breakpoint organizers for resource working sets.
 * 
 * @since 3.1
 */
public class WorkingSetBreakpointOrganizer extends AbstractBreakpointOrganizerDelegate implements IPropertyChangeListener {
    
    IWorkingSetManager fWorkingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
    
    /**
     * Constructs a working set breakpoint organizer. Listens for changes in
     * working sets and fires property change notification.
     */
    public WorkingSetBreakpointOrganizer() {    
        fWorkingSetManager.addPropertyChangeListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#getCategories(org.eclipse.debug.core.model.IBreakpoint)
     */
    public IAdaptable[] getCategories(IBreakpoint breakpoint) {
    	List result = new ArrayList();
        List parents = new ArrayList();
        IResource res = breakpoint.getMarker().getResource();
        parents.add(res);
        while (res != null) {
            res = res.getParent();
            if (res != null) {
                parents.add(res);
            }
        }
        IWorkingSet[] workingSets = fWorkingSetManager.getWorkingSets();
        for (int i = 0; i < workingSets.length; i++) {
            if (!IDebugUIConstants.BREAKPOINT_WORKINGSET_ID.equals(workingSets[i].getId())) {
		        IAdaptable[] elements = workingSets[i].getElements();
		        for (int j = 0; j < elements.length; j++) {
		            IResource resource = (IResource) elements[j].getAdapter(IResource.class);
		            if (resource != null) {
		                if (parents.contains(resource)) {
		                	result.add(new WorkingSetCategory(workingSets[i]));
		                	break;
		                }
		            }
		        }
            }
        }
        return (IAdaptable[]) result.toArray(new IAdaptable[result.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#dispose()
     */
    public void dispose() {
        fWorkingSetManager.removePropertyChangeListener(this);
        fWorkingSetManager = null;
        super.dispose();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        IWorkingSet set = null;
        if (event.getNewValue() instanceof IWorkingSet) {
            set = (IWorkingSet) event.getNewValue();
        } else if (event.getOldValue() instanceof IWorkingSet) {
            set = (IWorkingSet) event.getOldValue();
        }
        if (set != null && !IDebugUIConstants.BREAKPOINT_WORKINGSET_ID.equals(set.getId())) {
            fireCategoryChanged(new WorkingSetCategory(set));
        }
    }
}
