package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.views.framelist.TreeFrame;
import org.eclipse.ui.views.framelist.TreeViewerFrameSource;

/**
 * Frame source for the resource navigator.
 */
public class NavigatorFrameSource extends TreeViewerFrameSource {
	
	private ResourceNavigator navigator;

/**
 * Constructs a new frame source for the specified resource navigator.
 * 
 * @param navigator the resource navigator
 */
public NavigatorFrameSource(ResourceNavigator navigator) {
	super(navigator.getTreeViewer());
	this.navigator = navigator;
}

/**
 * Returns a new frame.  This implementation extends the super implementation
 * by setting the frame's tool tip text to show the full path for the input
 * element.
 */
protected TreeFrame createFrame(Object input) {
	TreeFrame frame = super.createFrame(input);
	frame.setName(navigator.getFrameName(input));
	frame.setToolTipText(navigator.getFrameToolTipText(input));
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
