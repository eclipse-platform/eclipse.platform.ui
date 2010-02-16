/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.actions.DebugCommandAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Terminate and remove action.
 * 
 * @since 3.3
 */
public class TerminateAndRemoveAction extends DebugCommandAction {

    public String getText() {
        return ActionMessages.TerminateAndRemoveAction_0;
    }

    public String getHelpContextId() {
        return "org.eclipse.debug.ui.terminate_and_remove_action_context"; //$NON-NLS-1$
    }

    public String getId() {
        return "org.eclipse.debug.ui.debugview.popupMenu.terminateAndRemove"; //$NON-NLS-1$
    }

    public String getToolTipText() {
        return ActionMessages.TerminateAndRemoveAction_3;
    }

    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TERMINATE_AND_REMOVE);
    }

    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_AND_REMOVE);
    }

    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_AND_REMOVE);
    }

    protected Class getCommandType() {
		return ITerminateHandler.class;
	}

    protected void postExecute(IRequest request, Object[] targets) {
        IStatus status = request.getStatus();
        if(status == null || status.isOK()) {
            for (int i = 0; i < targets.length; i++) {
                ILaunch launch = DebugUIPlugin.getLaunch(targets[i]);
                if (launch != null)
                    DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);                   
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.actions.DebugCommandAction#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    public void runWithEvent(Event event) {
    	// prompt if invoked via keyboard shortcut (don't prompt when menu is used)
    	if (event.widget instanceof Tree) {
    		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        	if (window != null) {
    	    	if (!MessageDialog.openQuestion(window.getShell(), DebugUIViewsMessages.LaunchView_Terminate_and_Remove_1, DebugUIViewsMessages.LaunchView_Terminate_and_remove_selected__2)) {  
    				return;
    			}
        	}
    	}
    	super.runWithEvent(event);
    }
}
