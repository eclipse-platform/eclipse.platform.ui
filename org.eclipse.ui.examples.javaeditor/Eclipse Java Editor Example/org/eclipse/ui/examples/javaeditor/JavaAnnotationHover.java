package org.eclipse.ui.examples.javaeditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;

/** 
 * The JavaAnnotationHover provides the hover support for java editors.
 */
 
public class JavaAnnotationHover implements IAnnotationHover {

	/* (non-Javadoc)
	 * Method declared on IAnnotationHover
	 */
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		IDocument document= sourceViewer.getDocument();

		try {
			IRegion info= document.getLineInformation(lineNumber);
			return document.get(info.getOffset(), info.getLength());
		} catch (BadLocationException x) {
		}

		return null;
	}
}
