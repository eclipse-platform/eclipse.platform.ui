package org.eclipse.ui.examples.javaeditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.text.*;
import org.eclipse.swt.graphics.Point;

/**
 * Example implementation for an <code>ITextHover</code> which hovers over Java code.
 */
public class JavaTextHover implements ITextHover {

	/* (non-Javadoc)
	 * Method declared on ITextHover
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (hoverRegion != null) {
			try {
				if (hoverRegion.getLength() > -1)
					return textViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());
			} catch (BadLocationException x) {
			}
		}
		return "empty selection";
	}
	
	/* (non-Javadoc)
	 * Method declared on ITextHover
	 */
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection= textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y)
			return new Region(selection.x, selection.y);
		return new Region(offset, 0);
	}
}
