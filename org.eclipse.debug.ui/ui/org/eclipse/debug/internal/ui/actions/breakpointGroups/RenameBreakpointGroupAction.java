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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

/**
 * An action which renames a breakpoint group.
 */
public class RenameBreakpointGroupAction extends AbstractBreakpointGroupAction {
	
	public RenameBreakpointGroupAction() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		String[] groups= getSelectedGroups();
		for (int i = 0; i < groups.length; i++) {
		    String group= groups[i];
		    InputDialog dialog= new InputDialog(fView.getViewSite().getShell(), "Rename Group", "Specify the new name for the group:", group, null);
			if (dialog.open() != Window.OK) {
				return;
			}
			String newGroup = dialog.getValue();
	        IBreakpoint[] breakpoints = getBreakpoints(group);
			for (int j = 0; j < breakpoints.length; j++) {
				try {
					breakpoints[j].setGroup(newGroup);
				} catch (CoreException e) {
				}
			}
        }
	}

}
