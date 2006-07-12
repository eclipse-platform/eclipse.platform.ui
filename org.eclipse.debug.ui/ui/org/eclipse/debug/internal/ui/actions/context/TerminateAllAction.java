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

 
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousTerminateAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Terminates all launches.
 */
public class TerminateAllAction extends AbstractDebugContextAction {
	

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#doAction(java.lang.Object)
     */
    protected void doAction(Object element) {
        if (element instanceof ILaunch) {
            ILaunch launch = (ILaunch) element;
            if (!launch.isTerminated() && DebugPlugin.getDefault().getLaunchManager().isRegistered(launch)) {
                IAsynchronousTerminateAdapter killer = (IAsynchronousTerminateAdapter) launch.getAdapter(IAsynchronousTerminateAdapter.class);
                if (killer != null) 
                    killer.terminate(element, new ActionRequestMonitor());
            }
        }
    }
	
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#isEnabledFor(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
     */
	protected void isEnabledFor(Object element, IBooleanRequestMonitor monitor) {
        
        //not really async here because we don't need to ask the target if the launch has terminated...
        // will this ever be called??? update is overridden in this class...
        
        if (element instanceof ILaunch) {
            ILaunch launch = (ILaunch) element;
            if (!launch.isTerminated() && DebugPlugin.getDefault().getLaunchManager().isRegistered(launch)) {
                monitor.setResult(true);
            } else {
            	monitor.setResult(false);
            }
        }
        monitor.done();
    }


    /**
	 * Update the action enablement based on the launches present in
	 * the launch manager. selection is unused and can be <code>null</code>.
	 * @see org.eclipse.debug.internal.ui.actions.AbstractDebugActionDelegate#update(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	protected void update(ISelection selection) {
		ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches= lManager.getLaunches();
		for (int i= 0; i< launches.length; i++) {
			ILaunch launch= launches[i];
			if (!launch.isTerminated()) {
				setEnabled(true);
				return;
			}
		}
		setEnabled(false);
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
