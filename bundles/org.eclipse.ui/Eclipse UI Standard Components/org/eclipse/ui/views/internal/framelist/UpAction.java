package org.eclipse.ui.views.internal.framelist;

/**
 * Generic "Up" action which switches the viewer's input
 * to be the parent of the current input.
 * Enabled only when the current input has a parent.
 */
public class UpAction extends FrameAction {
public UpAction(FrameList frameList) {
	super(frameList);
	setText("&Up One Level");
	setHoverImageDescriptor(getImageDescriptor("clcl16/up_nav.gif"));
	setImageDescriptor(getImageDescriptor("elcl16/up_nav.gif"));
	setDisabledImageDescriptor(getImageDescriptor("dlcl16/up_nav.gif"));
	update();
}
Frame getParentFrame(int flags) {
	return getFrameList().getSource().getFrame(IFrameSource.PARENT_FRAME, flags);
}
String getToolTipText(Frame parentFrame) {
	if (parentFrame != null) {
		String text = parentFrame.getToolTipText();
		if (text != null && text.length() > 0) {
			return "Up to " + text;
		}
	}
	return "Up";

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
