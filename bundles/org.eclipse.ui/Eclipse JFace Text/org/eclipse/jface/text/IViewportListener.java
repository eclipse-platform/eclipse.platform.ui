package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * Registered with a text viewer, viewport listeners are
 * informed about changes of a text viewer's viewport. The view port is that 
 * portion of the viewer's document which is visible in the viewer. <p>
 * Clients may implement this interface.
 *
 * @see ITextViewer 
 */
public interface IViewportListener {
	
	/**
	 * Informs about viewport changes. The given vertical position
	 * is the new vertical scrolling offset measured in pixels.
	 */
	void viewportChanged(int verticalOffset);
}
