package org.eclipse.ui.views.framelist;

/**********************************************************************
Copyright (c) 2000, 2001, 2002, International Business Machines Corp and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
 
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
		setHoverImageDescriptor(getImageDescriptor("clcl16/backward_nav.gif")); //$NON-NLS-1$
		setImageDescriptor(getImageDescriptor("elcl16/backward_nav.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(getImageDescriptor("dlcl16/backward_nav.gif")); //$NON-NLS-1$
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
				return FrameListMessages.format("Back.toolTipOneArg", new Object[] { text }); //$NON-NLS-1$
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