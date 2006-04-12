/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousDropToFrameAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Action delegate which performs a drop to frame.
 */
public class DropToFrameAction extends AbstractDebugContextAction {
    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#doAction(java.lang.Object)
     */
    protected void doAction(Object element) {
        if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            IAsynchronousDropToFrameAdapter dropper = (IAsynchronousDropToFrameAdapter) adaptable.getAdapter(IAsynchronousDropToFrameAdapter.class);
            if (dropper != null) 
                dropper.dropToFrame(element, new ActionRequestMonitor());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#isEnabledFor(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
     */
    protected void isEnabledFor(Object element, IBooleanRequestMonitor monitor) {
        if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            IAsynchronousDropToFrameAdapter dropper = (IAsynchronousDropToFrameAdapter) adaptable.getAdapter(IAsynchronousDropToFrameAdapter.class);
            if (dropper != null) {
                dropper.canDropToFrame(element, monitor);
            } else {
            	notSupported(monitor);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getText()
     */
    public String getText() {
        return ActionMessages.DropToFrameAction_0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getHelpContextId()
     */
    public String getHelpContextId() {
        return "drop_to_frame_action_context"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getId()
     */
    public String getId() {
        return "org.eclipse.debug.ui.debugview.toolbar.dropToFrame"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getToolTipText()
     */
    public String getToolTipText() {
        return ActionMessages.DropToFrameAction_3;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getHoverImageDescriptor()
     */
    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_DROP_TO_FRAME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_DROP_TO_FRAME);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getDisabledImageDescriptor()
	 */
	public ImageDescriptor getDisabledImageDescriptor() {
		return null;
	}
}
