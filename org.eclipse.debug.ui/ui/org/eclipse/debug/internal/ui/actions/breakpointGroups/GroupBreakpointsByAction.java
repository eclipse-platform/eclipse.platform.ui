/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *     Patrick Chuong (Texas Instruments) - Improve usability of the breakpoint view (Bug 238956)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointOrganizer;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointOrganizerManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

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
		menu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				Menu m = (Menu)e.widget;
				MenuItem[] items = m.getItems();
				for (int i=0; i < items.length; i++) {
					items[i].dispose();
				}
				fillMenu(m);
			}
		});		
		return menu;
    }

 	/**
	 * Fill pull down menu with the "group by" options
	 */
	private void fillMenu(Menu menu) {
		// determine which item should be checked
		IBreakpointOrganizer[] organizers = fView.getBreakpointOrganizers();					
		
		boolean none = false;
		boolean advanced = false;
		IBreakpointOrganizer organizer = null;
		if (organizers == null || organizers.length == 0) {
			none = true;
		} else if (organizers.length > 1) {
			advanced = true;
		} else {
			organizer = organizers[0];
		}
		
        int accel = 1;
        // Add hard-coded action for flat breakpoints list
        IAction action = new GroupBreakpointsAction(null, fView);
        addAccel(accel, action, BreakpointGroupMessages.GroupBreakpointsByAction_0); 
        accel++;
        action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_VIEW_BREAKPOINTS));
        action.setChecked(none);
        ActionContributionItem item= new ActionContributionItem(action);
        item.fill(menu, -1);

		// Add actions for each contributed organizer
	    List actions = getActions(accel);
        accel = accel + actions.size();
        Iterator actionIter = actions.iterator();
	    while (actionIter.hasNext()) {
			GroupBreakpointsAction bpAction = (GroupBreakpointsAction) actionIter.next();
			bpAction.setChecked(bpAction.getOrganizer().equals(organizer));
			item= new ActionContributionItem(bpAction);
			item.fill(menu, -1);
	    }
	                    
        // advanced action
        AdvancedGroupBreakpointsByAction advancedAction = new AdvancedGroupBreakpointsByAction(fView);
        addAccel(accel, advancedAction,BreakpointGroupMessages.GroupBreakpointsByAction_1); 
        advancedAction.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_HIERARCHICAL));
        advancedAction.setChecked(advanced);
		item= new ActionContributionItem(advancedAction);
		item.fill(menu, -1);
	}
    
    public List getActions(int accel) {
        List actions= new ArrayList();
        IBreakpointOrganizer[] organizers = BreakpointOrganizerManager.getDefault().getOrganizers();
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
