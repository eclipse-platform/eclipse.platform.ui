/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.framelist;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Generic "Back" action which goes back one frame,
 */
public class BackAction extends FrameAction {

    /**
     * Constructs a new action for the specified frame list.
     * 
     * @param frameList the frame list
     */
    public BackAction(FrameList frameList) {
        super(frameList);
        setText(FrameListMessages.getString("Back.text")); //$NON-NLS-1$
        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
        setImageDescriptor(images
                .getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
        setDisabledImageDescriptor(images
                .getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED));
        WorkbenchHelp.setHelp(this, IFrameListHelpContextIds.BACK_ACTION);
        update();
    }

    private Frame getPreviousFrame() {
        FrameList list = getFrameList();
        return list.getFrame(list.getCurrentIndex() - 1);
    }

    private String getToolTipText(Frame previousFrame) {
        if (previousFrame != null) {
            String text = previousFrame.getToolTipText();
            if (text != null && text.length() > 0) {
                return FrameListMessages.format(
                        "Back.toolTipOneArg", new Object[] { text }); //$NON-NLS-1$
            }
        }
        return FrameListMessages.getString("Back.toolTip"); //$NON-NLS-1$
    }

    /**
     * Calls <code>back()</code> on the frame list.
     */
    public void run() {
        getFrameList().back();
    }

    /**
     * Updates this action's enabled state and tool tip text.
     * This action is enabled only when there is a previous frame in the frame list.
     * The tool tip text is "Back to " plus the tool tip text for the previous frame.
     */
    public void update() {
        super.update();
        Frame previousFrame = getPreviousFrame();
        setEnabled(previousFrame != null);
        setToolTipText(getToolTipText(previousFrame));
    }

}