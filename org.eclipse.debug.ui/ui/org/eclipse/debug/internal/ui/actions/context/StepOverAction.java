/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Pawel Piech - bug 134177
 *******************************************************************************/

package org.eclipse.debug.internal.ui.actions.context;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.jface.resource.ImageDescriptor;

public class StepOverAction extends StepIntoAction {
	
	public StepOverAction() {
		setActionDefinitionId("org.eclipse.debug.ui.commands.StepOver"); //$NON-NLS-1$
	}
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.StepIntoAction#getText()
     */
    public String getText() {
        return ActionMessages.StepOverAction_0;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.StepIntoAction#getDisabledImageDescriptor()
     */
    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_STEP_OVER);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.StepIntoAction#getHelpContextId()
     */
    public String getHelpContextId() {
        return "step_over_action_context"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.StepIntoAction#getHoverImageDescriptor()
     */
    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_STEP_OVER);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.StepIntoAction#getId()
     */
    public String getId() {
        return "org.eclipse.debug.ui.debugview.toolbar.stepOver"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.StepIntoAction#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_STEP_OVER);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.StepIntoAction#getToolTipText()
     */
    public String getToolTipText() {
        return ActionMessages.StepOverAction_3;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.StepIntoAction#checkCapability(org.eclipse.debug.core.model.IStep, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
     */
    protected void checkCapability(Object element, IBooleanRequestMonitor monitor) {
        if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            IAsynchronousStepAdapter steppy = (IAsynchronousStepAdapter) adaptable.getAdapter(IAsynchronousStepAdapter.class);
            if (steppy != null) {
                steppy.canStepOver(element, monitor);
            } else {
            	notSupported(monitor);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.StepIntoAction#stepAction(org.eclipse.debug.core.model.IStep)
     */
    protected void stepAction(Object element) {
        if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            IAsynchronousStepAdapter steppy = (IAsynchronousStepAdapter) adaptable.getAdapter(IAsynchronousStepAdapter.class);
            if (steppy != null) 
                steppy.stepOver(element, new ActionRequestMonitor());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.StepIntoAction#getStatusMessage()
     */
    protected String getStatusMessage() {
        return ActionMessages.StepOverActionDelegate_Exceptions_occurred_attempting_to_step_over_the_frame_2;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.StepIntoAction#getErrorDialogMessage()
     */
    protected String getErrorDialogMessage() {
        return ActionMessages.StepOverActionDelegate_Step_over_failed_1;
    }
}
