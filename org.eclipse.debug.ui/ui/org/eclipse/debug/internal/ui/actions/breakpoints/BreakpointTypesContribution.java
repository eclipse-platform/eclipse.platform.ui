/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpoints;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.debug.internal.ui.actions.ToggleBreakpointsTargetManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Dynamic menu contribution that shows available breakpoint types from 
 * toggleBreakpointsTargetFactories extension point.
 * 
 * @since 3.5
 */
public class BreakpointTypesContribution extends CompoundContributionItem implements IWorkbenchContribution {
    
    private class SelectTargetAction extends Action {
        private final Set fPossibleIDs;
        private final String fID;
        SelectTargetAction(String name, Set possibleIDs, String ID) {
            super(name, AS_RADIO_BUTTON);
            fID = ID;
            fPossibleIDs = possibleIDs;
        }

        public void run() {
            if (isChecked()) {
                // Note: setPreferredTarget is not declared on the
                // IToggleBreakpontsTargetManager interface.
                ToggleBreakpointsTargetManager.getDefault().setPreferredTarget(fPossibleIDs, fID);
            }
        }
    }
 
    private IServiceLocator fServiceLocator;

    private static IContributionItem[] NO_BREAKPOINT_TYPES_CONTRIBUTION_ITEMS = new IContributionItem[] { 
    	new ContributionItem() {
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.NONE);
				item.setEnabled(false);
				item.setText(Messages.BreakpointTypesContribution_0);
			}
	
			public boolean isEnabled() {
				return false;
			}
    	}
    };
    
    protected IContributionItem[] getContributionItems() {
        IWorkbenchPart part = null;
        ISelection selection = null;
        
        ISelectionService selectionService = 
            (ISelectionService)fServiceLocator.getService(ISelectionService.class);
        if (selectionService != null) {
            selection = selectionService.getSelection();
        }
        IPartService partService = (IPartService)fServiceLocator.getService(IPartService.class);
        if (partService != null) {
            part = partService.getActivePart();
        }

        // If no part or selection, disable all.
        if (part == null || selection == null) {
            return NO_BREAKPOINT_TYPES_CONTRIBUTION_ITEMS;
        }
        
        // Get breakpoint toggle target IDs.
        IToggleBreakpointsTargetManager manager = DebugUITools.getToggleBreakpointsTargetManager(); 
        Set enabledIDs = manager.getEnabledToggleBreakpointsTargetIDs(part, selection);
        String preferredId = manager.getPreferredToggleBreakpointsTargetID(part, selection);

        List actions = new ArrayList(enabledIDs.size());
        for (Iterator i = enabledIDs.iterator(); i.hasNext();) {
            String id = (String) i.next();
            Action action = new SelectTargetAction(manager.getToggleBreakpointsTargetName(id), enabledIDs, id);
            if (id.equals(preferredId)) {
                action.setChecked(true);
            }
            actions.add(action);
        }
        
        if ( enabledIDs.isEmpty() ) {
            return NO_BREAKPOINT_TYPES_CONTRIBUTION_ITEMS;
        }
        
        IContributionItem[] items = new IContributionItem[enabledIDs.size()];
        for (int i = 0; i < actions.size(); i++) {
            items[i] = new ActionContributionItem((Action) actions.get(i));
        }
        return items;
    }
    
    public void initialize(IServiceLocator serviceLocator) {
        fServiceLocator = serviceLocator;
    }
    
}
