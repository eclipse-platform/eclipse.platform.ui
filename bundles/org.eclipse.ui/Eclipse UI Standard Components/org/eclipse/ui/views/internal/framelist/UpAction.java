package org.eclipse.ui.views.internal.framelist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * Generic "Up" action which switches the viewer's input
 * to be the parent of the current input.
 * Enabled only when the current input has a parent.
 *
 * @deprecated This has been promoted to API and will be removed for 2.0.  
 *   Use the corresponding class in package org.eclipse.ui.views.framelist instead.
 */
public class UpAction extends FrameAction {
public UpAction(FrameList frameList) {
	super(frameList);
	setText(FrameListMessages.getString("Up.text")); //$NON-NLS-1$
	setHoverImageDescriptor(getImageDescriptor("clcl16/up_nav.gif"));//$NON-NLS-1$
	setImageDescriptor(getImageDescriptor("elcl16/up_nav.gif"));//$NON-NLS-1$
	setDisabledImageDescriptor(getImageDescriptor("dlcl16/up_nav.gif"));//$NON-NLS-1$
	update();
}
Frame getParentFrame(int flags) {
	return getFrameList().getSource().getFrame(IFrameSource.PARENT_FRAME, flags);
}
String getToolTipText(Frame parentFrame) {
	if (parentFrame != null) {
		String text = parentFrame.getToolTipText();
		if (text != null && text.length() > 0) {
			return FrameListMessages.format("Up.toolTipOneArg", new Object[] {text}); //$NON-NLS-1$
		}
	}
	return FrameListMessages.getString("Up.toolTip"); //$NON-NLS-1$

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
public void update() {
	super.update();
	Frame parentFrame = getParentFrame(0);
	setEnabled(parentFrame != null);
	setToolTipText(getToolTipText(parentFrame));
}
}
