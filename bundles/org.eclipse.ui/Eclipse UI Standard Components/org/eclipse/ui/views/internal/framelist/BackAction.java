package org.eclipse.ui.views.internal.framelist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * Generic "Back" action, which calls <code>back()</code> on the frame list when run.
 * Enabled only when there is a frame after the current frame.
 * 
 * @deprecated This has been promoted to API and will be removed for 2.0.  
 *   Use the corresponding class in package org.eclipse.ui.views.framelist instead.
 */
public class BackAction extends FrameAction {
public BackAction(FrameList frameList) {
	super(frameList);
	setText(FrameListMessages.getString("Back.text")); //$NON-NLS-1$
	setHoverImageDescriptor(getImageDescriptor("clcl16/backward_nav.gif"));//$NON-NLS-1$
	setImageDescriptor(getImageDescriptor("elcl16/backward_nav.gif"));//$NON-NLS-1$
	setDisabledImageDescriptor(getImageDescriptor("dlcl16/backward_nav.gif"));//$NON-NLS-1$
	update();
}
Frame getPreviousFrame() {
	FrameList list = getFrameList();
	return list.getFrame(list.getCurrentIndex() - 1);
}
String getToolTipText(Frame previousFrame) {
	if (previousFrame != null) {
		String text = previousFrame.getToolTipText();
		if (text != null && text.length() > 0) {
			return FrameListMessages.format("Back.toolTipOneArg", new Object[] {text}); //$NON-NLS-1$
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
public void update() {
	super.update();
	Frame previousFrame = getPreviousFrame();
	setEnabled(previousFrame != null);
	setToolTipText(getToolTipText(previousFrame));
}
protected void updateEnabledState() {
	setEnabled(getFrameList().getCurrentIndex() > 0);
}
protected void updateToolTip() {
	FrameList list = getFrameList();
	Frame frame = list.getFrame(list.getCurrentIndex()-1);
	if (frame != null) {
		String text = frame.getToolTipText();
		if (text != null && text.length() > 0) {
			setToolTipText(FrameListMessages.format("Back.toolTipOneArg", new Object[] {text})); //$NON-NLS-1$
			return;
		}
	}
	setToolTipText(FrameListMessages.getString("Back.toolTip")); //$NON-NLS-1$
}
}
