/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainerFactoryManager;
import org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointOrganizer;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * 
 */
public class GroupBreakpointsByAction extends AbstractBreakpointsViewAction implements IMenuCreator {

	private IAction fAction= null;
	
	public GroupBreakpointsByAction() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
     */
    public Menu getMenu(Control parent) {
        // Never called
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
     */
    public Menu getMenu(Menu parent) {
		Menu menu = new Menu(parent);
		fillMenu(menu);
		return menu;
    }
    
	/**
	 * Fill pull down menu with the "group by" options
	 */
	private void fillMenu(Menu menu) {
        int accel = 1;
        // Add hard-coded action for flat breakpoints list
        IAction action = new GroupBreakpointsAction(null, fView);
        addAccel(accel, action, BreakpointGroupMessages.getString("GroupBreakpointsByAction.0")); //$NON-NLS-1$
        accel++;
        action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_VIEW_BREAKPOINTS));
        ActionContributionItem item= new ActionContributionItem(action);
        item.fill(menu, -1);

		// Add actions for each contributed orgranizer
	    List actions = getActions(accel);
        accel = accel + actions.size();
        Iterator actionIter = actions.iterator();
	    while (actionIter.hasNext()) {
			item= new ActionContributionItem((IAction) actionIter.next());
			item.fill(menu, -1);
	    }
	                    
        // advanced action
        AdvancedGroupBreakpointsByAction advancedAction = new AdvancedGroupBreakpointsByAction(fView);
        addAccel(accel, advancedAction,BreakpointGroupMessages.getString("GroupBreakpointsByAction.1")); //$NON-NLS-1$
        advancedAction.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_HIERARCHICAL));
		item= new ActionContributionItem(advancedAction);
		item.fill(menu, -1);
	}
    
    public List getActions(int accel) {
        List actions= new ArrayList();
        IBreakpointOrganizer[] organizers = BreakpointContainerFactoryManager.getDefault().getOrganizers();
        for (int i = 0; i < organizers.length; i++) {
        	IBreakpointOrganizer organizer = organizers[i];
            IAction action = new GroupBreakpointsAction(organizer, fView);
            addAccel(accel, action, organizer.getLabel());
            accel++;
            action.setImageDescriptor(organizer.getImageDescriptor());
            actions.add(action);
        }        
        return actions;
    }
    
    private void addAccel(int accel, IAction action, String label) {
        StringBuffer actionLabel= new StringBuffer();
        if (accel != 10) {
            if (accel < 10) {
                // add the numerical accelerators 1 through 9
                actionLabel.append('&');
            }
            actionLabel.append(accel);
        } else {
            actionLabel.append("1&0"); //$NON-NLS-1$
        }
        accel++;
        actionLabel.append(' ');
        actionLabel.append(label);
        action.setText(actionLabel.toString());        
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	    if (action != fAction) {
	        action.setMenuCreator(this);
	        fAction= action;
	    }
	}
}
