package org.eclipse.ui.views.internal.framelist;

/**
 * Generic "Forward" action, which calls <code>forward()</code> on the frame list when run.
 * Enabled only when there is a frame after the current frame.
 */
public class ForwardAction extends FrameAction {
public ForwardAction(FrameList frameList) {
	super(frameList);
	setText("&Forward");
	setHoverImageDescriptor(getImageDescriptor("clcl16/forward_nav.gif"));
	setImageDescriptor(getImageDescriptor("elcl16/forward_nav.gif"));
	setDisabledImageDescriptor(getImageDescriptor("dlcl16/forward_nav.gif"));
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
			return "Forward to " + text;
		}
	}
	return "Forward";
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
