/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationPresentation;
import org.eclipse.jface.text.source.ImageUtilities;

/**
 * Annotation used to represent the projection of a master document onto a
 * <code>ProjectionDocument</code>. A projection annotation can be either
 * expanded or collapsed. If expanded it corresponds to a segment of the
 * projection document. If collapsed, it represents a region of the master
 * document that does not have a corresponding segment in the projection
 * document.
 * <p>
 * 
 * @since 3.0
 */
public class ProjectionAnnotation extends Annotation implements IAnnotationPresentation {
	
	private static class DisplayDisposeRunnable implements Runnable {

		public void run() {
			if (fgCollapsedImage != null) {
				fgCollapsedImage.dispose();
				fgCollapsedImage= null;
			}
			if (fgExpandedImage != null) {
				fgExpandedImage.dispose();
				fgExpandedImage= null;
			}
		}
	}

	/**
	 * The type of projection annotations.
	 */
	public static final String TYPE= "org.eclipse.projection"; //$NON-NLS-1$
		
	
	private static final int COLOR= SWT.COLOR_DARK_GRAY;
	private static Image fgCollapsedImage;
	private static Image fgExpandedImage;
	
	
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
			
	private void drawRangeIndication(GC gc, Canvas canvas, Rectangle r) {
		final int MARGIN= 3;
		Color fg= gc.getForeground();
		gc.setForeground(canvas.getDisplay().getSystemColor(COLOR));
		
		gc.setLineWidth(1);
		gc.drawLine(r.x + 4, r.y + 12, r.x + 4, r.y + r.height - MARGIN);
		gc.drawLine(r.x + 4, r.y + r.height - MARGIN, r.x + r.width - MARGIN, r.y + r.height - MARGIN);
		gc.setForeground(fg);
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationPresentation#paint(org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
	 */
	public void paint(GC gc, Canvas canvas, Rectangle rectangle) {
		Image image= getImage(canvas.getDisplay());
		if (image != null) {
			ImageUtilities.drawImage(image, gc, canvas, rectangle, SWT.CENTER, SWT.TOP);
			if (fIsRangeIndication)
				drawRangeIndication(gc, canvas, rectangle);
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationPresentation#getLayer()
	 */
	public int getLayer() {
		return IAnnotationPresentation.DEFAULT_LAYER;
	}
	
	private Image getImage(Display display) {
		initializeImages(display);
		return isCollapsed() ? fgCollapsedImage : fgExpandedImage;
	}
	
	private void initializeImages(Display display) {
		if (fgCollapsedImage == null) {
			
			ImageDescriptor descriptor= ImageDescriptor.createFromFile(ProjectionAnnotation.class, "images/collapsed.gif"); //$NON-NLS-1$
			fgCollapsedImage= descriptor.createImage(display);
			descriptor= ImageDescriptor.createFromFile(ProjectionAnnotation.class, "images/expanded.gif"); //$NON-NLS-1$
			fgExpandedImage= descriptor.createImage(display);
			
			display.disposeExec(new DisplayDisposeRunnable());
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
