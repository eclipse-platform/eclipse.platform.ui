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
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.jface.resource.ImageDescriptor;

public class StepReturnAction extends StepIntoAction {
    
    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_STEP_RETURN);
    }

    public String getHelpContextId() {
        return "step_return_action_context"; //$NON-NLS-1$
    }

    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_STEP_RETURN);
    }

    public String getId() {
        return "org.eclipse.debug.ui.debugview.toolbar.stepReturn"; //$NON-NLS-1$
    }

    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_STEP_RETURN);
    }

    public String getToolTipText() {
        return ActionMessages.StepReturnAction_2;
    }

    public String getText() {
        return ActionMessages.StepReturnAction_3;
    }

    protected boolean checkCapability(IStep element) {
        return element.canStepReturn();
    }

    protected void stepAction(IStep element) throws DebugException {
        element.stepReturn();
    }

    protected String getStatusMessage() {
        return ActionMessages.StepReturnActionDelegate_Exceptions_occurred_attempting_to_run_to_return_of_the_frame__2;
    }

    protected String getErrorDialogMessage() {
        return ActionMessages.StepReturnActionDelegate_Run_to_return_failed__1;
    }
}
