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

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.commands.provisional.IStepFiltersCommand;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

public class ToggleStepFiltersAction extends DebugCommandAction {
	
	public ImageDescriptor getDisabledImageDescriptor() {
		return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TOGGLE_STEP_FILTERS);
	}

	public String getHelpContextId() {
		return "step_with_filters_action_context"; //$NON-NLS-1$
	}

	public ImageDescriptor getHoverImageDescriptor() {
		return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TOGGLE_STEP_FILTERS);
	}

	public String getId() {
		return "org.eclipse.debug.ui.actions.ToggleStepFilters"; //$NON-NLS-1$
	}

	public ImageDescriptor getImageDescriptor() {
		return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TOGGLE_STEP_FILTERS);
	}

	public String getText() {
		return ActionMessages.ToggleStepFiltersAction_0;
	}

	public String getToolTipText() {		
		return ActionMessages.ToggleStepFiltersAction_0;
	}

	protected boolean getInitialEnablement() {
		return true;
	}

	protected Class getCommandType() {
		return IStepFiltersCommand.class;
	}

    public void run() {
    	DebugUITools.setUseStepFilters(!DebugUITools.isUseStepFilters());
    }
	
    public int getStyle() {
    	return AS_CHECK_BOX;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.commands.actions.DebugCommandAction#contextActivated(org.eclipse.jface.viewers.ISelection, org.eclipse.ui.IWorkbenchPart, org.eclipse.debug.internal.ui.contexts.IDebugContextService)
	 */
	public void contextActivated(ISelection context, IWorkbenchPart part) {
		if (context.isEmpty()) {
			setEnabled(true);
		} else {
			super.contextActivated(context, part);
		}
	}
    
    
}
