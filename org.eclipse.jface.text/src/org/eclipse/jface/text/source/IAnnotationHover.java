package org.eclipse.jface.text.source;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */



/**
 * Provides the information to be displayed in a hover popup window
 * which appears over the presentation area of annotations. Clients
 * may implement this interface.
 */
public interface IAnnotationHover {
		
	/**
	 * Returns the text which should be presented in the a
	 * hover popup window. This information is requested based on
	 * the specified line number.
	 *
	 * @param sourceViewer the source viewer this hover is registered with
	 * @param lineNumber the line number for which information is requested
	 * @return the requested information or <code>null</code> if no such information exists
	 */
	String getHoverInfo(ISourceViewer sourceViewer, int lineNumber);
}