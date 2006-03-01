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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDropToFrame;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Action delegate which performs a drop to frame.
 */
public class DropToFrameAction extends AbstractDebugContextAction {
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#doAction(java.lang.Object)
     */
    protected void doAction(Object element) throws DebugException {
        if (element instanceof IDropToFrame) {
            IDropToFrame dropToFrame = (IDropToFrame) element;
            if (dropToFrame.canDropToFrame()) {
                dropToFrame.dropToFrame();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#isEnabledFor(java.lang.Object)
     */
    protected boolean isEnabledFor(Object element) {
        return element instanceof IDropToFrame && ((IDropToFrame) element).canDropToFrame();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getTarget(java.lang.Object)
     */
    protected Object getTarget(Object selectee) {
        if (selectee instanceof IDropToFrame) {
            return selectee;
        }
        if (selectee instanceof IAdaptable) {
            return ((IAdaptable) selectee).getAdapter(IDropToFrame.class);
        }
        return null;
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
     * @see org.eclipse.debug.internal.ui.actions.context.AbstractDebugContextAction#getDisabledImageDescriptor()
     */
    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_DROP_TO_FRAME);
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
}
