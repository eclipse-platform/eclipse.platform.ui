package org.eclipse.ui.views.internal.framelist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * Generic "Go Into" action which switches the viewer's input
 * to be the currently selected container.
 * Enabled only when the current selection is a single container.
 * 
 * @deprecated This has been promoted to API and will be removed for 2.0.  
 *   Use the corresponding class in package org.eclipse.ui.views.framelist instead.
 */
public class GoIntoAction extends FrameAction {
public GoIntoAction(FrameList frameList) {
	super(frameList);
	setText(FrameListMessages.getString("GoInto.text")); //$NON-NLS-1$
	setToolTipText(FrameListMessages.getString("GoInto.toolTip")); //$NON-NLS-1$
	update();
}
Frame getSelectionFrame(int flags) {
	return getFrameList().getSource().getFrame(IFrameSource.SELECTION_FRAME, flags);
}
/**
 * Calls <code>gotoFrame</code> on the frame list with a frame
 * representing the currently selected container.
 */
public void run() {
	Frame selectionFrame = getSelectionFrame(IFrameSource.FULL_CONTEXT);
	if (selectionFrame != null) {
		getFrameList().gotoFrame(selectionFrame);
	}
}
public void update() {
	super.update();
	Frame selectionFrame = getSelectionFrame(0);
	setEnabled(selectionFrame != null);
}
}
