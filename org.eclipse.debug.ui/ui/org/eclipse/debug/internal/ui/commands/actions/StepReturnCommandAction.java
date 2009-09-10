/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.debug.core.commands.IStepReturnHandler;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.actions.DebugCommandAction;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Step return action.
 * 
 * @since 3.3
 */
public class StepReturnCommandAction extends DebugCommandAction {
    
	
	public StepReturnCommandAction() {
		setActionDefinitionId("org.eclipse.debug.ui.commands.StepReturn"); //$NON-NLS-1$	
	}
	
    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_STEP_RETURN);
    }

    public String getHelpContextId() {
        return "org.eclipse.debug.ui.step_return_action_context"; //$NON-NLS-1$
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

	protected Class getCommandType() {
		return IStepReturnHandler.class;
	}

}
