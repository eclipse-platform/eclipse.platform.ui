package org.eclipse.ui.views.internal.framelist;

/**
 * Generic "Back" action, which calls <code>back()</code> on the frame list when run.
 * Enabled only when there is a frame after the current frame.
 */
public class BackAction extends FrameAction {
public BackAction(FrameList frameList) {
	super(frameList);
	setText("&Back");
	setHoverImageDescriptor(getImageDescriptor("clcl16/backward_nav.gif"));
	setImageDescriptor(getImageDescriptor("elcl16/backward_nav.gif"));
	setDisabledImageDescriptor(getImageDescriptor("dlcl16/backward_nav.gif"));
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
			return "Back to " + text;
		}
	}
	return "Back";
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
			setToolTipText("Back to " + text);
			return;
		}
	}
	setToolTipText("Back");
}
}
