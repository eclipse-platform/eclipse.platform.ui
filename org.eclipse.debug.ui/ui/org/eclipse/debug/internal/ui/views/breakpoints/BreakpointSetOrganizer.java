/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.AbstractBreakpointOrganizerDelegate;
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
public class BreakpointSetOrganizer extends AbstractBreakpointOrganizerDelegate implements IPropertyChangeListener, IBreakpointsListener {
    
    IWorkingSetManager fWorkingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
    /**
     * Constructs a working set breakpoint organizer. Listens for changes in
     * working sets and fires property change notification.
     */
    public BreakpointSetOrganizer() {
        fWorkingSetManager.addPropertyChangeListener(this);
        DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
        DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#getCategories(org.eclipse.debug.core.model.IBreakpoint)
     */
    public IAdaptable[] getCategories(IBreakpoint breakpoint) {
    	List result = new ArrayList();
        IWorkingSet[] workingSets = fWorkingSetManager.getWorkingSets();
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
        fWorkingSetManager.removePropertyChangeListener(this);
        fWorkingSetManager = null;
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
        IWorkingSet[] workingSets = fWorkingSetManager.getWorkingSets();
        for (int i = 0; i < workingSets.length; i++) {
            IWorkingSet set = workingSets[i];
            if (IInternalDebugUIConstants.ID_BREAKPOINT_WORKINGSET.equals(set.getId())) { 
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
            if (IInternalDebugUIConstants.ID_BREAKPOINT_WORKINGSET.equals(set.getId())) { 
                name = set.getName();
            }
        }
        DebugUIPlugin.getDefault().getPluginPreferences().setValue(IInternalDebugUIConstants.MEMENTO_BREAKPOINT_WORKING_SET_NAME, name);
    }    
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#canRemove(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public boolean canRemove(IBreakpoint breakpoint, IAdaptable category) {
        if (category instanceof WorkingSetCategory) {
            WorkingSetCategory wsc = (WorkingSetCategory) category;
            return IInternalDebugUIConstants.ID_BREAKPOINT_WORKINGSET.equals(wsc.getWorkingSet().getId());
        }
        return super.canRemove(breakpoint, category);
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#canAdd(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public boolean canAdd(IBreakpoint breakpoint, IAdaptable category) {
        if (category instanceof WorkingSetCategory) {
            WorkingSetCategory wsc = (WorkingSetCategory) category;
            return IInternalDebugUIConstants.ID_BREAKPOINT_WORKINGSET.equals(wsc.getWorkingSet().getId());
        }
        return super.canAdd(breakpoint, category);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#addBreakpoint(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public void addBreakpoint(IBreakpoint breakpoint, IAdaptable category) {
        if (category instanceof WorkingSetCategory) {
            IWorkingSet workingSet = ((WorkingSetCategory) category).getWorkingSet();
            IAdaptable[] elements = workingSet.getElements();
            IAdaptable[] newElements = new IAdaptable[elements.length + 1];
            System.arraycopy(elements, 0, newElements, 0, elements.length);
            newElements[elements.length] = breakpoint;
            workingSet.setElements(newElements);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#removeBreakpoint(org.eclipse.debug.core.model.IBreakpoint, org.eclipse.core.runtime.IAdaptable)
     */
    public void removeBreakpoint(IBreakpoint breakpoint, IAdaptable category) {
        if (category instanceof WorkingSetCategory) {
            IWorkingSet workingSet = ((WorkingSetCategory) category).getWorkingSet();
            IAdaptable[] elements = workingSet.getElements();
            List list = new ArrayList();
            for (int i = 0; i < elements.length; i++) {
                IAdaptable adaptable = elements[i];
                if (!adaptable.equals(breakpoint)) {
                    list.add(adaptable);
                }
            }
            workingSet.setElements((IAdaptable[]) list.toArray(new IAdaptable[list.size()]));
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IBreakpointOrganizerDelegate#getCategories()
     */
    public IAdaptable[] getCategories() {
        IWorkingSet[] workingSets = fWorkingSetManager.getWorkingSets();
        List all = new ArrayList();
        for (int i = 0; i < workingSets.length; i++) {
            IWorkingSet set = workingSets[i];
            if (IInternalDebugUIConstants.ID_BREAKPOINT_WORKINGSET.equals(set.getId())) {
                all.add(new WorkingSetCategory(set));
            }
        }
        return (IAdaptable[]) all.toArray(new IAdaptable[all.size()]);
    }
}
