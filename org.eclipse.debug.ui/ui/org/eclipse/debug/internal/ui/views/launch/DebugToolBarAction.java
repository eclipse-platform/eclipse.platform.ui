/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Action that controls whether Debug actions are shown in Launch view.
 * 
 * @since 3.8
 */
class DebugToolBarAction extends Action {

	private final LaunchView fLaunchView;
	private final boolean fDebugViewToolbar;
    private final boolean fDebugToolbarActionSet;

	/**
	 * Creates a new action to set the debug view mode.
	 * 
	 * @param view Reference to the debug view.
	 * @param debugViewToolbar Causes action to show toolbar in Debug view.
	 * @param debugActionSet Causes action to show toolbar in top level Window 
	 * toolbar..
	 */
	public DebugToolBarAction(LaunchView view, boolean debugViewToolbar, boolean debugActionSet) {
		super(IInternalDebugCoreConstants.EMPTY_STRING, AS_RADIO_BUTTON);
		fLaunchView = view;
		fDebugViewToolbar = debugViewToolbar;
		fDebugToolbarActionSet = debugActionSet;
		
		if (fDebugViewToolbar && fDebugToolbarActionSet) {
            setText(LaunchViewMessages.DebugToolBarAction_Both_label);
            setToolTipText(LaunchViewMessages.DebugToolBarAction_Both_tooltip);  
            setDescription(LaunchViewMessages.DebugToolBarAction_Both_description);  
            PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.DEBUG_TOOLBAR_BOTH_ACTION);
		} else if (fDebugViewToolbar) {
            setText(LaunchViewMessages.DebugToolBarAction_View_label);
            setToolTipText(LaunchViewMessages.DebugToolBarAction_View_tooltip);  
            setDescription(LaunchViewMessages.DebugToolBarAction_View_description);  
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.DEBUG_TOOLBAR_VIEW_ACTION);
		} else if (fDebugToolbarActionSet) {
            setText(LaunchViewMessages.DebugToolBarAction_Window_label);
            setToolTipText(LaunchViewMessages.DebugToolBarAction_Window_tooltip);  
            setDescription(LaunchViewMessages.DebugToolBarAction_Window_description);  
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.DEBUG_TOOLBAR_WINDOW_ACTION);
		} 	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fLaunchView.setDebugToolbarInView(fDebugViewToolbar); 

		IWorkbenchPage page = fLaunchView.getSite().getPage(); 
        
		if (fDebugToolbarActionSet) {
            page.showActionSet(IDebugUIConstants.DEBUG_TOOLBAR_ACTION_SET);
		} else {
            page.hideActionSet(IDebugUIConstants.DEBUG_TOOLBAR_ACTION_SET);
		}
	}	
	
	/**
     * @return Returns whether debug toolbar is shown in view by this action.
	 */
	public boolean getDebugViewToolbar() {
	    return fDebugViewToolbar;
	}

    /**
     * @return Returns whether debug toolbar action set is shown by this action.
     */
    public boolean getDebugToolbarActionSet() {
        return fDebugToolbarActionSet;
    }
}

