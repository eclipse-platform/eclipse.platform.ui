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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousDisconnectAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;

public class DisconnectAction extends AbstractDebugContextAction {

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#doAction(java.lang.Object)
     */
    protected void doAction(Object element) {
        if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            IAsynchronousDisconnectAdapter disconnect = (IAsynchronousDisconnectAdapter) adaptable.getAdapter(IAsynchronousDisconnectAdapter.class);
            if (disconnect != null) 
                disconnect.disconnect(element, new ActionRequestMonitor());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#isEnabledFor(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
     */
    protected void isEnabledFor(Object element, IBooleanRequestMonitor monitor) {
        if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            IAsynchronousDisconnectAdapter disconnect = (IAsynchronousDisconnectAdapter) adaptable.getAdapter(IAsynchronousDisconnectAdapter.class);
            if (disconnect != null) 
                disconnect.canDisconnect(element, monitor);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getStatusMessage()
     */
    protected String getStatusMessage() {
        return ActionMessages.DisconnectActionDelegate_Exceptions_occurred_attempting_to_disconnect__2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getErrorDialogMessage()
     */
    protected String getErrorDialogMessage() {
        return ActionMessages.DisconnectActionDelegate_Disconnect_failed_1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getText()
     */
    public String getText() {
        return ActionMessages.DisconnectAction_0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getHelpContextId()
     */
    public String getHelpContextId() {
        return "disconnect_action_context"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getId()
     */
    public String getId() {
        return "org.eclipse.debug.ui.debugview.toolbar.disconnect"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getToolTipText()
     */
    public String getToolTipText() {
        return ActionMessages.DisconnectAction_3;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getDisabledImageDescriptor()
     */
    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_DISCONNECT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getHoverImageDescriptor()
     */
    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_DISCONNECT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_DISCONNECT);
    }
}
