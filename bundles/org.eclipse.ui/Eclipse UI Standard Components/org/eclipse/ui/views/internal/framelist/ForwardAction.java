package org.eclipse.ui.views.internal.framelist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * Generic "Forward" action, which calls <code>forward()</code> on the frame list when run.
 * Enabled only when there is a frame after the current frame.
 * 
 * @deprecated This has been promoted to API and will be removed for 2.0.  
 *   Use the corresponding class in package org.eclipse.ui.views.framelist instead.
 */
public class ForwardAction extends FrameAction {
public ForwardAction(FrameList frameList) {
	super(frameList);
	setText(FrameListMessages.getString("Forward.text")); //$NON-NLS-1$
	setHoverImageDescriptor(getImageDescriptor("clcl16/forward_nav.gif"));//$NON-NLS-1$
	setImageDescriptor(getImageDescriptor("elcl16/forward_nav.gif"));//$NON-NLS-1$
	setDisabledImageDescriptor(getImageDescriptor("dlcl16/forward_nav.gif"));//$NON-NLS-1$
	update();
}
Frame getNextFrame() {
	FrameList list = getFrameList();
	return list.getFrame(list.getCurrentIndex() + 1);
}
String getToolTipText(Frame nextFrame) {
	if (nextFrame != null) {
		String text = nextFrame.getToolTipText();
		if (text != null && text.length() > 0) {
			return FrameListMessages.format("Forward.toolTipOneArg", new Object[] {text}); //$NON-NLS-1$
		}
	}
	return FrameListMessages.getString("Forward.toolTip"); //$NON-NLS-1$
}
/**
 * Calls <code>forward()</code> on the frame list.
 */
public void run() {
	getFrameList().forward();
}
public void update() {
	super.update();
	Frame nextFrame = getNextFrame();
	setEnabled(nextFrame != null);
	setToolTipText(getToolTipText(nextFrame));
}
}
