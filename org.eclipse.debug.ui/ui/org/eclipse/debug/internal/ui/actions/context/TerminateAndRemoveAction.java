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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.jface.resource.ImageDescriptor;

public class TerminateAndRemoveAction extends AbstractDebugContextAction {

    protected void doAction(Object element) throws DebugException {
        LaunchView.terminateAndRemove(element);
    }

    protected boolean isEnabledFor(Object element) {
        if (element instanceof ITerminate) {
            ITerminate terminate = (ITerminate) element;
            // do not want to terminate an attach launch that does not
            // have termination enabled
            return terminate.canTerminate() || terminate.isTerminated();
        }
        return false;
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
