/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.debug.core.commands.IRestartHandler;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.actions.DebugCommandAction;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Handler for the Restart action
 * 
 * @since 3.6
 */
public class RestartCommandAction extends DebugCommandAction {

    public RestartCommandAction() {
        setActionDefinitionId("org.eclipse.debug.ui.commands.Restart"); //$NON-NLS-1$
    }
    
    protected Class getCommandType() {
        return IRestartHandler.class;
    }

    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_RESTART);
    }

    public String getHelpContextId() {
        return "org.eclipse.debug.ui.restart_action_context"; //$NON-NLS-1$
    }

    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_RESTART);
    }

    public String getId() {
        return "org.eclipse.debug.ui.actions.Restart"; //$NON-NLS-1$
    }

    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_RESTART);
    }

    public String getText() {
        return ActionMessages.RestartCommandAction__text;
    }

    public String getToolTipText() {
        return ActionMessages.RestartCommandAction_tooltip;
    }

    
}
