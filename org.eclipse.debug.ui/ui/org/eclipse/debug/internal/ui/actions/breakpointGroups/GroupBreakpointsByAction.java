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

import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.actions.ShowBreakpointsByAction;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainerFactoryManager;
import org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointContainerFactory;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * 
 */
public class GroupBreakpointsByAction extends AbstractBreakpointsViewAction implements IMenuCreator {

	private boolean fFillMenu= true;
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
		// Add actions for each contributed factory
	    Iterator actionIter = getActions().iterator();
	    while (actionIter.hasNext()) {
			ActionContributionItem item= new ActionContributionItem((IAction) actionIter.next());
			item.fill(menu, -1);
	    }
	    
	    Separator separator = new Separator();
	    separator.fill(menu, -1);
        
	    // Add hard-coded actions
        IAction action = new GroupBreakpointsAction(null, fView);
        action.setText(ActionMessages.getString("GroupBreakpointsByAction.0")); //$NON-NLS-1$
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(menu, -1);
        
        ShowBreakpointsByAction advancedAction = new ShowBreakpointsByAction();
        advancedAction.setText(ActionMessages.getString("GroupBreakpointsByAction.1")); //$NON-NLS-1$
        advancedAction.init(fView);
		item= new ActionContributionItem(advancedAction);
		item.fill(menu, -1);
	}
    
    public List getActions() {
        List actions= new ArrayList();
        IBreakpointContainerFactory[] factories = BreakpointContainerFactoryManager.getDefault().getFactories();
        for (int i = 0; i < factories.length; i++) {
        	IBreakpointContainerFactory factory= factories[i];
            IAction action = new GroupBreakpointsAction(factory, fView);
            StringBuffer actionLabel= new StringBuffer();
    		if (i >= 0 && i < 10) {
    			//add the numerical accelerator
    			actionLabel.append('&');
    			actionLabel.append(i);
    			actionLabel.append(' ');
    		}
			actionLabel.append(factory.getLabel());
			action.setText(actionLabel.toString());
            actions.add(action);
        }        
        return actions;
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
