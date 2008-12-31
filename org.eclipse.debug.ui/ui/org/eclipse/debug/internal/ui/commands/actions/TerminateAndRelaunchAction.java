/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.actions.RelaunchActionDelegate;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Action which terminates a launch and then re-launches it.
 */
public class TerminateAndRelaunchAction extends DebugCommandAction {
	
	class Participant implements ICommandParticipant {
		
		private Object[] fTargets;

		public Participant(Object[] targets) {
			fTargets = targets;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.commands.actions.ICommandParticipant#requestDone(org.eclipse.debug.core.commands.IRequest)
		 */
		public void requestDone(IRequest request) {
			if (request.getStatus() == null || request.getStatus().isOK()) {
				DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
		            public void run() {
		                // Must be run in the UI thread since the launch can require
		                // prompting to proceed
		            	for (int i = 0; i < fTargets.length; i++) {
		            		ILaunch launch = RelaunchActionDelegate.getLaunch(fTargets[i]);
		            		RelaunchActionDelegate.relaunch(launch.getLaunchConfiguration(), launch.getLaunchMode());
						}
		            }
		        });	
			}
		}
		
	}

	protected ICommandParticipant getCommandParticipant(Object[] targets) {
		return new Participant(targets);
	}

	protected Class getCommandType() {
		return ITerminateHandler.class;
	}

	public void debugContextChanged(DebugContextEvent event) {
		ISelection context = event.getContext();
		if (context instanceof IStructuredSelection) {
			Object[] elements = ((IStructuredSelection)context).toArray();
			for (int i = 0; i < elements.length; i++) {
				if (!canRelaunch(elements[i])) {
					setEnabled(false);
					return;
				}
			} 
		}
		super.debugContextChanged(event);
	}

    protected boolean canRelaunch(Object element) {
    	ILaunch launch = RelaunchActionDelegate.getLaunch(element);
    	if (launch != null) {
    		ILaunchConfiguration configuration = launch.getLaunchConfiguration();
    		if (configuration != null) {
    			return LaunchConfigurationManager.isVisible(configuration);
    		}
    	}
		return false; 
    }

    public String getActionDefinitionId() {
        return ActionMessages.TerminateAndRelaunchAction_0;
    }

    public String getHelpContextId() {
        return "org.eclipse.debug.ui.terminate_and_relaunch_action_context"; //$NON-NLS-1$
    }

    public String getId() {
        return "org.eclipse.debug.ui.debugview.popupMenu.TerminateAndRelaunch"; //$NON-NLS-1$
    }

    public String getText() {
        return ActionMessages.TerminateAndRelaunchAction_3;
    }

    public String getToolTipText() {
        return ActionMessages.TerminateAndRelaunchAction_4;
    }

    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TERMINATE_AND_RELAUNCH);
    }

    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_AND_RELAUNCH);
    }

    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_AND_RELAUNCH);
    }
}
