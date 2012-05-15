/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.debug.core.commands.IDisconnectHandler;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.DebugCommandAction;
import org.eclipse.jface.resource.ImageDescriptor;
/**
 * Disconnect action.
 * 
 * @since 3.3
 */
public class DisconnectCommandAction extends DebugCommandAction{

    public DisconnectCommandAction() {
        setActionDefinitionId("org.eclipse.debug.ui.commands.Disconnect"); //$NON-NLS-1$
    }
    
    public String getText() {
        return ActionMessages.DisconnectAction_0;
    }

    public String getHelpContextId() {
        return "org.eclipse.debug.ui.disconnect_action_context"; //$NON-NLS-1$
    }

    public String getId() {
        return "org.eclipse.debug.ui.debugview.toolbar.disconnect"; //$NON-NLS-1$
    }

    public String getToolTipText() {
        return ActionMessages.DisconnectAction_3;
    }

    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_DISCONNECT);
    }

    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_DISCONNECT);
    }

    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_DISCONNECT);
    }

	protected Class getCommandType() {
		return IDisconnectHandler.class;
	}
}
