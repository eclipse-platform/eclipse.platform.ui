package org.eclipse.jface.text;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */


/**
 * Registered with a text viewer, viewport listeners are
 * informed about changes of text viewer's viewport, i.e. that 
 * portion of the viewer's document that is visible in the viewer. <p>
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
