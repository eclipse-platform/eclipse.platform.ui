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
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * ShowEmptyGroupsAction
 */
public class ShowEmptyGroupsAction extends Action {
    
    private StructuredViewer fViewer;
    private ViewerFilter fFilter = new EmptyGroupFilter();
    
    /**
     * Filters empty breakpoint containers.
     */
    class EmptyGroupFilter extends ViewerFilter {

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (element instanceof BreakpointContainer) {
                BreakpointContainer container = (BreakpointContainer) element;
                return container.getBreakpoints().length > 0;
            }
            return true;
        }
        
    }
    
    /**
     * Constructs a new action to toggle the empty groups filter
     */
    public ShowEmptyGroupsAction(StructuredViewer viewer) {
        super(BreakpointGroupMessages.getString("ShowEmptyGroupsAction.0"), IAction.AS_CHECK_BOX); //$NON-NLS-1$
        fViewer = viewer;
        setChecked(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_SHOW_EMPTY_GROUPS));
        run();
    }

    public void run() {
        if (isChecked()) {
            fViewer.removeFilter(fFilter);
        } else {
            fViewer.addFilter(fFilter);
        }
        DebugUIPlugin.getDefault().getPreferenceStore().setValue(IInternalDebugUIConstants.PREF_SHOW_EMPTY_GROUPS, isChecked());
    }
}
