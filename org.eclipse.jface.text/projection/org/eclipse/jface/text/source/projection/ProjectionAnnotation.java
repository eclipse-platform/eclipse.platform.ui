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
package org.eclipse.jface.text.source.projection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationPresentation;

/**
 * Annotation used to represent the projection of a master document onto a
 * <code>ProjectionDocument</code>. A projection annotation can be either
 * expanded or collapsed. If expanded it corresponds to a segment of the
 * projection document. If collapsed, it represents a region of the master
 * document that does not have a corresponding segment in the projection
 * document.
 * <p>
 * Internal class. Do not use. Public only for testing purposes.
 * 
 * @since 3.0
 */
public class ProjectionAnnotation extends Annotation implements IAnnotationPresentation {
	
	/**
	 * The type of projection annotations.
	 */
	public static final String TYPE= "org.eclipse.projection";
		
	private static final boolean PLUS= false;
	private static final int COLOR= SWT.COLOR_DARK_GRAY;
	
	private static final int OUTER_MARGIN= 1;
	private static final int INNER_MARGIN= 1;
	private static final int PIXELS= 1;
	private static final int LEGS= 2;
	private static final int MIDDLE= PIXELS + INNER_MARGIN + LEGS;
	private static final int SIZE= 2 * MIDDLE + PIXELS;

	/** The state of this annotation */
	private boolean fIsCollapsed= false;
	/** Indicates whether this annotation should be painted as range */
	private boolean fIsRangeIndication= false;
	
	/** 
	 * Creates a new projection annotation.
	 */
	public ProjectionAnnotation() {
		this(false);
	}
	
	public ProjectionAnnotation(boolean isCollapsed) {
		super(TYPE, false, null);
		fIsCollapsed= isCollapsed;
	}
	
	public void setRangeIndication(boolean rangeIndication) {
		fIsRangeIndication= rangeIndication;
	}
	
	private void paintPlus(GC gc, Canvas canvas, Rectangle rectangle) {
		Color fg= gc.getForeground();
		gc.setForeground(canvas.getDisplay().getSystemColor(COLOR));
					

		Rectangle r= new Rectangle(rectangle.x + OUTER_MARGIN, rectangle.y + OUTER_MARGIN, SIZE -1 , SIZE -1);
		gc.drawRectangle(r);
		gc.drawLine(r.x + PIXELS + INNER_MARGIN, r.y + MIDDLE, r.x + r.width - PIXELS - INNER_MARGIN , r.y + MIDDLE);
		if (fIsCollapsed) {
			gc.drawLine(r.x + MIDDLE, r.y + PIXELS + INNER_MARGIN, r.x + MIDDLE, r.y + r.height - PIXELS - INNER_MARGIN);
		} else {
			gc.drawLine(r.x + MIDDLE, r.y + r.height, r.x + MIDDLE, rectangle.y + rectangle.height - OUTER_MARGIN);
			gc.drawLine(r.x + MIDDLE, rectangle.y + rectangle.height - OUTER_MARGIN, r.x + r.width - INNER_MARGIN, rectangle.y + rectangle.height - OUTER_MARGIN);
		}
			
		gc.setForeground(fg);		
	}
	
	private int[] computePolygon(Rectangle rectangle) {
		
		int leftX= rectangle.x;
		int rightX= rectangle.x + rectangle.width;
		int middleX= (leftX + rightX)/2;
		
		int upperY= rectangle.y;
		int lowerY= rectangle.y + rectangle.height;
		int middleY= (upperY + lowerY)/2;
		
		if (isCollapsed()) {
			Point upperLeft= new Point(leftX, upperY - 1);
			Point middleRight= new Point(rightX, middleY);
			Point lowerLeft= new Point(leftX, lowerY);
			return new int[] {upperLeft.x, upperLeft.y, middleRight.x, middleRight.y, lowerLeft.x, lowerLeft.y};
		} else {
			Point middleLeft= new Point(leftX, upperY);
			Point middleRight= new Point(rightX, upperY);
			Point lowerMiddle= new Point(middleX, lowerY);
			return new int[] {middleLeft.x, middleLeft.y, middleRight.x , middleRight.y, lowerMiddle.x, lowerMiddle.y };
		}
	}
	
	private Rectangle computeRectangle(Rectangle rectangle, int lineHeight) {		
		final int MARGIN= 2;
		int leftX= rectangle.x + MARGIN;
		int length= rectangle.width - 2*MARGIN;
		int yDelta= (lineHeight - length)/2;
		int upperY= rectangle.y + yDelta;
		return new Rectangle(leftX, upperY, length, length);
	}
	
	private Point paintTriangle(GC gc, Canvas canvas, Rectangle rectangle) {
		Point endPoint= null;
		int lineHeight= gc.getFontMetrics().getHeight();
		int[] polygon= computePolygon(computeRectangle(rectangle, lineHeight));
		if (isCollapsed()) {
			Color bg= gc.getBackground();
			gc.setBackground(canvas.getDisplay().getSystemColor(COLOR));
			gc.fillPolygon(polygon);
			gc.setBackground(bg);
		} else {
			Color fg= gc.getForeground();
			gc.setForeground(canvas.getDisplay().getSystemColor(COLOR));
			gc.drawPolygon(polygon);
			gc.setForeground(fg);
			endPoint= new Point(polygon[polygon.length -2], polygon[polygon.length -1] + 2);
		}
		return endPoint;
	}
	
	private void paintRangeIndication(GC gc, Canvas canvas, Rectangle rectangle, Point startPoint) {
		final int MARGIN= 3;
		Color fg= gc.getForeground();
		gc.setForeground(canvas.getDisplay().getSystemColor(COLOR));
		gc.setLineWidth(1);
		gc.drawLine(startPoint.x, startPoint.y, startPoint.x, rectangle.y + rectangle.height - MARGIN);
		gc.drawLine(startPoint.x, rectangle.y + rectangle.height - MARGIN, rectangle.x + rectangle.width - MARGIN, rectangle.y + rectangle.height - MARGIN);
		gc.setForeground(fg);
	}
	
	/*
	 * @see org.eclipse.jface.text.source.Annotation#paint(org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
	 */
	public void paint(GC gc, Canvas canvas, Rectangle rectangle) {
		if (PLUS)
			paintPlus(gc, canvas, rectangle);
		else {
			Point p= paintTriangle(gc, canvas, rectangle);
			if (p != null && fIsRangeIndication)
				paintRangeIndication(gc, canvas, rectangle, p);
		}
	}
	
	/**
	 * Returns the state of this annotation.
	 * 
	 * @return <code>true</code> if collapsed 
	 */
	public boolean isCollapsed() {
		return fIsCollapsed;
	}

	/**
	 * Marks this annotation as being collapsed.
	 */
	public void markCollapsed() {
		fIsCollapsed= true;
	}

	/**
	 * Marks this annotation as being unfolded.
	 */
	public void markExpanded() {
		fIsCollapsed= false;
	}
}
