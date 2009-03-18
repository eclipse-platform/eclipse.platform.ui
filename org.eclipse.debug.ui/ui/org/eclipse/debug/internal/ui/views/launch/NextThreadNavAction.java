/*******************************************************************************
  * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     IBM Corporation - updated to use command constants 
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.viewers.model.NavigateModelAction;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;

import org.eclipse.debug.ui.DebugUITools;

/**
 * Navigates to the next suspended thread in the Debug view.
 *
 * @since 3.5
 */
class NextThreadNavAction extends NavigateModelAction {
	public NextThreadNavAction(TreeModelViewer viewer) {
		super(viewer, false);
        setText(ActionMessages.NextThreadNavAction_name);
        setImageDescriptor(DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_NEXT_THREAD));
        setDisabledImageDescriptor(DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_NEXT_THREAD));
        setId(DebugUIPlugin.getUniqueIdentifier() + ".NextThreadNavigateAction"); //$NON-NLS-1$
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.NEXT_THREAD_NAVIGATE_ACTION);
		setActionDefinitionId(IWorkbenchCommandConstants.NAVIGATE_NEXT);
	}
}
