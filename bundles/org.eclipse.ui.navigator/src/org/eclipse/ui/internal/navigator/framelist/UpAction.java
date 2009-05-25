/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.framelist;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Generic "Up" action which goes to the parent frame for the current frame.
 * @since 3.4
 */
public class UpAction extends FrameAction {

	private static final String ID = "org.eclipse.ui.framelist.up"; //$NON-NLS-1$

	/**
     * Constructs a new action for the specified frame list.
     * 
     * @param frameList the frame list
     */
    public UpAction(FrameList frameList) {
        super(frameList);
        setId(ID);
        setText(FrameListMessages.Up_text);
        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
        setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_UP));
        setDisabledImageDescriptor(images
                .getImageDescriptor(ISharedImages.IMG_TOOL_UP_DISABLED));
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IFrameListHelpContextIds.UP_ACTION);
        update();
    }

    Frame getParentFrame(int flags) {
        return getFrameList().getSource().getFrame(IFrameSource.PARENT_FRAME,
                flags);
    }

    String getToolTipText(Frame parentFrame) {
        if (parentFrame != null) {
            String text = parentFrame.getToolTipText();
            if (text != null && text.length() > 0) {
                return NLS.bind(FrameListMessages.Up_toolTipOneArg, text);
            }
        }
        return FrameListMessages.Up_toolTip;

    }

    /**
     * Calls <code>gotoFrame</code> on the frame list with a frame
     * representing the parent of the current input.
     */
    public void run() {
        Frame parentFrame = getParentFrame(IFrameSource.FULL_CONTEXT);
        if (parentFrame != null) {
            getFrameList().gotoFrame(parentFrame);
        }
    }

    /**
     * Updates this action's enabled state and tool tip text.
     * This action is enabled only when there is a parent frame for the current
     * frame in the frame list.
     * The tool tip text is "Up to " plus the tool tip text for the parent
     * frame.
     */
    public void update() {
        super.update();
        Frame parentFrame = getParentFrame(0);
        setEnabled(parentFrame != null);
        setToolTipText(getToolTipText(parentFrame));
    }
}
