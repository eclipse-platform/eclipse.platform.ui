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
package org.eclipse.debug.internal.ui.actions.context;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousTerminateAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.jface.resource.ImageDescriptor;

public class TerminateAndRemoveAction extends AbstractDebugContextAction {

    
    class TerminateAndRemoveMonitor extends ActionRequestMonitor {
        private Object fElement;
        TerminateAndRemoveMonitor(Object element) {
            fElement = element;
        }
        public void done() {
            if(getStatus() == null) {
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
    protected void doAction(Object element) {
        if (element instanceof IAdaptable) {
            IAsynchronousTerminateAdapter adapter = (IAsynchronousTerminateAdapter) ((IAdaptable)element).getAdapter(IAsynchronousTerminateAdapter.class);
            if (adapter != null) 
                adapter.terminate(element, new TerminateAndRemoveMonitor(element));
        }
    }

    
    protected void isEnabledFor(Object element, IBooleanRequestMonitor monitor) {
        if (element instanceof IAdaptable) {
            IAsynchronousTerminateAdapter adapter = (IAsynchronousTerminateAdapter) ((IAdaptable)element).getAdapter(IAsynchronousTerminateAdapter.class);
            if (adapter != null)
                adapter.canTerminate(element, monitor);
        }
    
    }

    protected String getStatusMessage() {
        return ActionMessages.TerminateAndRemoveActionDelegate_Exceptions_occurred_attempting_to_terminate_and_remove_2;
    }

    protected String getErrorDialogMessage() {
        return ActionMessages.TerminateAndRemoveActionDelegate_Terminate_and_remove_failed_1;
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

}
