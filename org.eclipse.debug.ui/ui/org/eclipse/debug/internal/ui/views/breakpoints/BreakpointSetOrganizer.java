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
import java.util.List;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.AbstractBreakpointOrganizer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

/**
 * Breakpoint organizers for breakpoint working sets.
 * 
 * @since 3.1
 */
public class BreakpointSetOrganizer extends AbstractBreakpointOrganizer implements IPropertyChangeListener, IBreakpointsListener {
    
    /**
     * Constructs a working set breakpoint organizer. Listens for changes in
     * working sets and fires property change notification.
     */
    public BreakpointSetOrganizer() {
        PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(this);
        DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
        DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#getCategories(org.eclipse.debug.core.model.IBreakpoint)
     */
    public IAdaptable[] getCategories(IBreakpoint breakpoint) {
    	List result = new ArrayList();
        IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
        IWorkingSet[] workingSets = manager.getWorkingSets();
        for (int i = 0; i < workingSets.length; i++) {
            IWorkingSet set = workingSets[i];
            if (IInternalDebugUIConstants.ID_BREAKPOINT_WORKINGSET.equals(set.getId())) {
	            IAdaptable[] elements = set.getElements();
	            for (int j = 0; j < elements.length; j++) {
	                IAdaptable adaptable = elements[j];
	                if (adaptable.equals(breakpoint)) {
	                	result.add(new WorkingSetCategory(set));
	                	break;
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
        PlatformUI.getWorkbench().getWorkingSetManager().removePropertyChangeListener(this);
        DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
        DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
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
        if (set != null && IInternalDebugUIConstants.ID_BREAKPOINT_WORKINGSET.equals(set.getId())) {
            fireCategoryChanged(new WorkingSetCategory(set));
        }
        if (event.getProperty().equals(IInternalDebugUIConstants.MEMENTO_BREAKPOINT_WORKING_SET_NAME)) {
            IWorkingSet defaultWorkingSet = getDefaultWorkingSet();
            if (defaultWorkingSet != null) {
                fireCategoryChanged(new WorkingSetCategory(defaultWorkingSet));
            } else {
                fireCategoryChanged(null);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint[])
     */
    public void breakpointsAdded(IBreakpoint[] breakpoints) {
        IWorkingSet set = getDefaultWorkingSet();
        if (set != null) {
            IAdaptable[] elements = set.getElements();
            IAdaptable[] newElements = new IAdaptable[elements.length + breakpoints.length];
            System.arraycopy(elements, 0, newElements, 0, elements.length);
            System.arraycopy(breakpoints, 0, newElements, elements.length, breakpoints.length);
            set.setElements(newElements);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
     */
    public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
        IWorkingSet[] workingSets = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
        for (int i = 0; i < workingSets.length; i++) {
            IWorkingSet set = workingSets[i];
            if (IInternalDebugUIConstants.ID_BREAKPOINT_WORKINGSET.equals(set.getId())) { //$NON-NLS-1$
                clean(set);
            }
        }
    }
    
    /**
     * Removes deleted breakpoints from the given working set.
     * 
     * @param workingSet breakpoint working set
     */
    private void clean(IWorkingSet workingSet) {
        IAdaptable[] elements = workingSet.getElements();
        IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
        boolean update = false;
        for (int i = 0; i < elements.length; i++) {
            IAdaptable adaptable = elements[i];
            if (adaptable instanceof IBreakpoint) {
                IBreakpoint breakpoint = (IBreakpoint) adaptable;
                if (!manager.isRegistered(breakpoint)) {
                    update = true;
                    elements[i] = null;
                }
            }
        }
        if (update) {
            List newElements = new ArrayList(elements.length);
            for (int i = 0; i < elements.length; i++) {
                IAdaptable adaptable = elements[i];
                if (adaptable != null) {
                    newElements.add(adaptable);
                }
            }
            workingSet.setElements((IAdaptable[]) newElements.toArray(new IAdaptable[newElements.size()]));
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
     */
    public void breakpointsChanged(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
    }
    
    /**
     * Returns the active default breakpoint working set, or <code>null</code>
     * if none.
     * 
     * @return the active default breakpoint working set, or <code>null</code>
     */
    public static IWorkingSet getDefaultWorkingSet() {
        IPreferenceStore preferenceStore = DebugUIPlugin.getDefault().getPreferenceStore();
        String name = preferenceStore.getString(IInternalDebugUIConstants.MEMENTO_BREAKPOINT_WORKING_SET_NAME);
        if (name != null) {
            return PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(name);
        }
        return null;
    }
    
    /**
     * Sets the active default breakpoint working set, or <code>null</code>
     * if none.
     * 
     * @param set default working set or <code>null</code>
     */
    public static void setDefaultWorkingSet(IWorkingSet set) {
        String name = ""; //$NON-NLS-1$
        if (set != null) {
            // only consider breakpoint working sets
            if (IInternalDebugUIConstants.ID_BREAKPOINT_WORKINGSET.equals(set.getId())) { //$NON-NLS-1$
                name = set.getName();
            }
        }
        DebugUIPlugin.getDefault().getPluginPreferences().setValue(IInternalDebugUIConstants.MEMENTO_BREAKPOINT_WORKING_SET_NAME, name);
    }    
}
