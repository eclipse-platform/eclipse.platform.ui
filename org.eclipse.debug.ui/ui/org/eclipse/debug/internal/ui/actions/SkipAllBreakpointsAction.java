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
package org.eclipse.debug.internal.ui.actions;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * An action which toggles the breakpoint manager's enablement.
 * This causes debug targets which honor the manager's enablement
 * to skip (not suspend for) all breakpoints. 
 */
public class SkipAllBreakpointsAction extends Action {
	
	public SkipAllBreakpointsAction() {
		super(ActionMessages.getString("SkipAllBreakpointsAction.0")); //$NON-NLS-1$
		setToolTipText(ActionMessages.getString("SkipAllBreakpointsAction.0")); //$NON-NLS-1$
		setDescription(ActionMessages.getString("SkipAllBreakpointsAction.2")); //$NON-NLS-1$
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_SKIP_BREAKPOINTS));
		WorkbenchHelp.setHelp(this, IDebugHelpContextIds.SKIP_ALL_BREAKPOINT_ACTION);
		updateActionCheckedState();
	}
	/**
	 * @see IAction#run()
	 */
	public void run(){
		IBreakpointManager manager = getBreakpointManager();
		manager.setEnabled(!manager.isEnabled());
	}
	
	/**
	 * Updates the action's checked state to be opposite the enabled
	 * state of the breakpoint manager.
	 */
	public void updateActionCheckedState() {
		setChecked(!getBreakpointManager().isEnabled());
	}
	
	/**
	 * Returns the global breakpoint manager.
	 * 
	 * @return the global breakpoint manager
	 */
	public static IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();
	}
}
