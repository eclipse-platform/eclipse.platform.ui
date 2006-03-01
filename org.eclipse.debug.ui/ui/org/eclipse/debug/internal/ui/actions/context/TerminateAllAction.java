/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.context;

 
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Terminates all launches.
 */
public class TerminateAllAction extends AbstractDebugContextAction {
	

	protected void doAction(Object element) throws DebugException {
		if (element instanceof ILaunch) {
			ILaunch launch = (ILaunch) element;
			if (!launch.isTerminated() && DebugPlugin.getDefault().getLaunchManager().isRegistered(launch)) {
				launch.terminate();
			}			
		}
	}
	
	/**
	 * Update the action enablement based on the launches present in
	 * the launch manager. selection is unused and can be <code>null</code>.
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#update(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	protected void update(IAction action, ISelection selection) {
		ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches= lManager.getLaunches();
		for (int i= 0; i< launches.length; i++) {
			ILaunch launch= launches[i];
			if (!launch.isTerminated()) {
				action.setEnabled(true);
				return;
			}
		}
		action.setEnabled(false);
	}

	protected IStructuredSelection getContext() {
		return new StructuredSelection(DebugPlugin.getDefault().getLaunchManager().getLaunches());
	}

    public String getHelpContextId() {
        return "terminate_all_action_context"; //$NON-NLS-1$
    }

    public String getId() {
        return "org.eclipse.debug.ui.debugview.popupMenu.terminateAll"; //$NON-NLS-1$
    }

    public String getText() {
        return ActionMessages.TerminateAllAction_2;
    }

    public String getToolTipText() {
        return ActionMessages.TerminateAllAction_3;
    }

    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TERMINATE_ALL);
    }

    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_ALL);
    }

    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_ALL);
    }
}
