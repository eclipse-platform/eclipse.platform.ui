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
 * Annotation used to represent the projection of a master document onto
 * a <code>ProjectionDocument</code>. A projection annotation can be either
 * expanded or collapsed. If expaned it corresponds to a fragment of the
 * projection document. If collapsed, it represents a region of the master document
 * that does not have a corresponding fragment in the projection document. <p>
 * Draws itself in a tree like fashion.<p>
 * This class if for internal use only.
 * 
 * @since 2.1
 */
public class ProjectionAnnotation extends Annotation {
	
	private static final int OUTER_MARGIN= 1;
	private static final int INNER_MARGIN= 1;
	private static final int PIXELS= 1;
	private static final int LEGS= 2;
	private static final int MIDDLE= PIXELS + INNER_MARGIN + LEGS;
	private static final int SIZE= 2 * MIDDLE + PIXELS;

	/** The range in the master document */
	private Position fProjectionRange;
	/** The state of this annotation */
	private boolean fIsFolded= false;
	
	/** 
	 * Creates a new projection annotation for the given range of the master document.
	 * 
	 * @param range the range.
	 */
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
	
	/**
	 * Toogles the state of this annotation and updates the given viewer accordingly.
	 * 
	 * @param viewer the viewer
	 */
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
	 * Returns the state of this annotation.
	 * 
	 * @return <code>true</code> if collapsed 
	 */
	public boolean isFolded() {
		return fIsFolded;
	}
}
