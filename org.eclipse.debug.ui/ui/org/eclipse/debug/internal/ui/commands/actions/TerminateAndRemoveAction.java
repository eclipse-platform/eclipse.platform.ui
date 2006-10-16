/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.commands.provisional.ITerminateCommand;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Terminate and remove action.
 * 
 * @since 3.3
 */
public class TerminateAndRemoveAction extends DebugCommandAction {

    
    class TerminateAndRemoveMonitor extends ActionRequestMonitor {
        private Object fElement;
        TerminateAndRemoveMonitor(Object element) {
            fElement = element;
        }
        public void done() {
            IStatus status = getStatus();
			if(status == null || status.isOK()) {
                ILaunch launch= null;
                if (fElement instanceof ILaunch) {
                    launch= (ILaunch) fElement;
                } else if (fElement instanceof IDebugElement) {
                    launch= ((IDebugElement) fElement).getLaunch();
                } else if (fElement instanceof IProcess) {
                    launch= ((IProcess) fElement).getLaunch();
                }   
                if (launch != null)
                    DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
            }
            super.done();
        }
        
    }

    public String getText() {
        return ActionMessages.TerminateAndRemoveAction_0;
    }

    public String getHelpContextId() {
        return "terminate_and_remove_action_context"; //$NON-NLS-1$
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
		return ITerminateCommand.class;
	}

	protected IAsynchronousRequestMonitor createStatusMonitor(Object target) {
		return new TerminateAndRemoveMonitor(target);
	}

    
}
