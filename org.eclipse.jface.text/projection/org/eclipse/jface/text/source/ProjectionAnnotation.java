/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;

/**
 * ProjectionAnnotation.java
 */
public class ProjectionAnnotation extends Annotation {
	
	private static final int OUTER_MARGIN= 1;
	private static final int INNER_MARGIN= 1;
	private static final int PIXELS= 1;
	private static final int LEGS= 2;
	private static final int MIDDLE= PIXELS + INNER_MARGIN + LEGS;
	private static final int SIZE= 2 * MIDDLE + PIXELS;

	
	private Position fProjectionRange;
	private boolean fIsFolded= false;
	
	
	public ProjectionAnnotation(Position range) {
		fProjectionRange= range;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.Annotation#paint(org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
	 */
	public void paint(GC gc, Canvas canvas, Rectangle rectangle) {
		Color fg= gc.getForeground();
		gc.setForeground(canvas.getDisplay().getSystemColor(SWT.COLOR_BLUE));
					

		Rectangle r= new Rectangle(rectangle.x + OUTER_MARGIN, rectangle.y + OUTER_MARGIN, SIZE -1 , SIZE -1);
		gc.drawRectangle(r);
		gc.drawLine(r.x + PIXELS + INNER_MARGIN, r.y + MIDDLE, r.x + r.width - PIXELS - INNER_MARGIN , r.y + MIDDLE);
		if (fIsFolded) {
			gc.drawLine(r.x + MIDDLE, r.y + PIXELS + INNER_MARGIN, r.x + MIDDLE, r.y + r.height - PIXELS - INNER_MARGIN);
		} else {
			gc.drawLine(r.x + MIDDLE, r.y + r.height, r.x + MIDDLE, rectangle.y + rectangle.height - OUTER_MARGIN);
			gc.drawLine(r.x + MIDDLE, rectangle.y + rectangle.height - OUTER_MARGIN, r.x + r.width - INNER_MARGIN, rectangle.y + rectangle.height - OUTER_MARGIN);
		}
			
		gc.setForeground(fg);
	}
	
	public void run(ITextViewer viewer) {
		
		if (viewer instanceof ProjectionSourceViewer) {
			ProjectionSourceViewer projectionViewer= (ProjectionSourceViewer) viewer;
			
			if (fIsFolded) {
				
				fIsFolded= false;
				projectionViewer.expand(fProjectionRange.getOffset(), fProjectionRange.getLength());
			
			} else {
				
				try {
					IDocument document= projectionViewer.getDocument();
					int line= document.getLineOfOffset(fProjectionRange.getOffset());
					int offset= document.getLineOffset(line + 1);
					
					int length= fProjectionRange.getLength() - (offset - fProjectionRange.getOffset());
					if (length > 0)	{
						fIsFolded= true;
						projectionViewer.collapse(offset, length);
					}
				} catch (BadLocationException x) {
				}
			}
		}
	}
	
	/**
	 * Returns the fIsFolded.
	 * @return boolean
	 */
	public boolean isFolded() {
		return fIsFolded;
	}
}
