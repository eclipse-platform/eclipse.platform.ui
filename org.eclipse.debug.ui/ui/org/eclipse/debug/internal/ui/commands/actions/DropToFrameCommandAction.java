/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.debug.core.commands.IDropToFrameHandler;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.actions.DebugCommandAction;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Drop to frame action.
 * 
 * @since 3.3
 */
public class DropToFrameCommandAction extends DebugCommandAction {

    public DropToFrameCommandAction() {
        setActionDefinitionId("org.eclipse.debug.ui.commands.DropToFrame"); //$NON-NLS-1$
    }
    
    public String getText() {
        return ActionMessages.DropToFrameAction_0;
    }

    public String getHelpContextId() {
        return "org.eclipse.debug.ui.drop_to_frame_action_context"; //$NON-NLS-1$
    }

    public String getId() {
        return "org.eclipse.debug.ui.debugview.toolbar.dropToFrame"; //$NON-NLS-1$
    }

    public String getToolTipText() {
        return ActionMessages.DropToFrameAction_3;
    }

    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_DROP_TO_FRAME);
    }

    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_DROP_TO_FRAME);
    }

	public ImageDescriptor getDisabledImageDescriptor() {
		return null;
	}

	protected Class getCommandType() {
		return IDropToFrameHandler.class;
	}
}
