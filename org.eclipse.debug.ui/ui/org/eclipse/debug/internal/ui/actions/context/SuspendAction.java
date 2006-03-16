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
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousSuspendResumeAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.jface.resource.ImageDescriptor;

public class SuspendAction extends AbstractDebugContextAction {

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#doAction(java.lang.Object)
     */
    protected void doAction(Object element) {
        if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            IAsynchronousSuspendResumeAdapter suspendResumer = (IAsynchronousSuspendResumeAdapter) adaptable.getAdapter(IAsynchronousSuspendResumeAdapter.class);
            if (suspendResumer != null) 
                suspendResumer.suspend(element, new ActionRequestMonitor());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#isEnabledFor(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
     */
    protected void isEnabledFor(Object element, IBooleanRequestMonitor monitor) {
        if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            IAsynchronousSuspendResumeAdapter suspendResumer = (IAsynchronousSuspendResumeAdapter) adaptable.getAdapter(IAsynchronousSuspendResumeAdapter.class);
            if (suspendResumer != null) 
                suspendResumer.canSuspend(element, monitor);
        }
        
    }


    protected String getStatusMessage() {
        return ActionMessages.SuspendActionDelegate_Exceptions_occurred_attempting_to_suspend__2;
    }

    protected String getErrorDialogMessage() {
        return ActionMessages.SuspendActionDelegate_Suspend_failed_1;
    }

    public String getText() {
        return ActionMessages.SuspendAction_0;
    }

    public String getHelpContextId() {
        return "suspend_action_context"; //$NON-NLS-1$
    }

    public String getId() {
        return "org.eclipse.debug.ui.debugview.toolbar.suspend"; //$NON-NLS-1$
    }

    public String getToolTipText() {
        return ActionMessages.SuspendAction_3;
    }

    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_SUSPEND);
    }

    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_SUSPEND);
    }

    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_SUSPEND);
    }
}
