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

public class StepIntoAction extends StepAction {
	
	public StepIntoAction() {
		setActionDefinitionId("org.eclipse.debug.ui.commands.StepInto"); //$NON-NLS-1$
	}

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.StepAction#checkCapability(org.eclipse.debug.core.model.IStep, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
     */
	protected void checkCapability(Object element, IBooleanRequestMonitor monitor) {
        if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            IAsynchronousStepAdapter steppy = (IAsynchronousStepAdapter) adaptable.getAdapter(IAsynchronousStepAdapter.class);
            if (steppy != null) {
                steppy.canStepInto(element, monitor);
            } else {
            	notSupported(monitor);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.StepAction#stepAction(org.eclipse.debug.core.model.IStep)
     */
	protected void stepAction(Object element) {
        if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            IAsynchronousStepAdapter steppy = (IAsynchronousStepAdapter) adaptable.getAdapter(IAsynchronousStepAdapter.class);
            if (steppy != null) 
                steppy.stepInto(element, new ActionRequestMonitor());
        }
	}

	/*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getStatusMessage()
	 */
	protected String getStatusMessage() {
		return ActionMessages.StepIntoActionDelegate_Exceptions_occurred_attempting_to_step_into_the_frame_2;
	}

	/*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return ActionMessages.StepIntoActionDelegate_Step_into_failed_1; 
	}

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getText()
     */
    public String getText() {
        return ActionMessages.StepIntoAction_0;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getHelpContextId()
     */
    public String getHelpContextId() {
        return "step_into_action_context"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getId()
     */
    public String getId() {
        return "org.eclipse.debug.ui.debugview.toolbar.stepInto"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getToolTipText()
     */
    public String getToolTipText() {
        return ActionMessages.StepIntoAction_3;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getDisabledImageDescriptor()
     */
    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_STEP_INTO);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getHoverImageDescriptor()
     */
    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_STEP_INTO);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_STEP_INTO);
    }
}
