/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;


import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.contextlaunching.LaunchingResourceManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.RelaunchLastAction;

/**
 * Re-launches the last run-mode launch
 * 
 * This menu item appears in the main 'Run' menu
 * 
 * @see RelaunchLastAction
 * @see DebugLastAction
 * @see ProfileLastAction
 */
public class RunLastAction extends RelaunchLastAction {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.RelaunchLastAction#getMode()
	 */
	public String getMode() {
		return ILaunchManager.RUN_MODE;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RelaunchLastAction#getLaunchGroupId()
	 */
	public String getLaunchGroupId() {
		return IDebugUIConstants.ID_RUN_LAUNCH_GROUP;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.RelaunchLastAction#getText()
	 */
	protected String getText() {
		if(LaunchingResourceManager.isContextLaunchEnabled()) {
			return ActionMessages.RunLastAction_1;
		}
		else {
			return ActionMessages.RunLastAction_0;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.RelaunchLastAction#getTooltipText()
	 */
	protected String getTooltipText() {
		return IInternalDebugCoreConstants.EMPTY_STRING;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RelaunchLastAction#getCommandId()
	 */
	protected String getCommandId() {
		return "org.eclipse.debug.ui.commands.RunLast"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RelaunchLastAction#getDescription()
	 */
	protected String getDescription() {
		if(LaunchingResourceManager.isContextLaunchEnabled()) {
			return ActionMessages.RunLastAction_2;
		}
		else {
			return ActionMessages.RunLastAction_3;
		}
	}	
}
