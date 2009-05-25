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
 * Generic "Back" action which goes back one frame,
 * @since 3.4
 */
public class BackAction extends FrameAction {

	private static final String ID = "org.eclipse.ui.framelist.back"; //$NON-NLS-1$
	
    /**
     * Constructs a new action for the specified frame list.
     * 
     * @param frameList the frame list
     */
    public BackAction(FrameList frameList) {
        super(frameList);
        setId(ID);
        setText(FrameListMessages.Back_text);
        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
        setImageDescriptor(images
                .getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
        setDisabledImageDescriptor(images
                .getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED));
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IFrameListHelpContextIds.BACK_ACTION);
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
                return NLS.bind(FrameListMessages.Back_toolTipOneArg, text);
            }
        }
        return FrameListMessages.Back_toolTip;
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
