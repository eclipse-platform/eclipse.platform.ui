package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.views.internal.framelist.TreeFrame;
import org.eclipse.ui.views.internal.framelist.TreeViewerFrameSource;

public class NavigatorFrameSource extends TreeViewerFrameSource {
	private ResourceNavigator navigator;
public NavigatorFrameSource(ResourceNavigator navigator) {
	super(navigator.getResourceViewer());
	this.navigator = navigator;
}
protected TreeFrame createFrame(Object input) {
	TreeFrame frame = super.createFrame(input);
	frame.setToolTipText(navigator.getToolTipText(input));
	return frame;
}
/**
 * Also updates the navigator's title.
 */
protected void frameChanged(TreeFrame frame) {
	super.frameChanged(frame);
	navigator.updateTitle();
}
}
