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

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Supports the configuration of projection capabilities for projection viewers.
 * <p>
 * API in progress. Do not yet use.
 * 
 * @since 3.0
 */
public class ProjectionSupport {
	
	
	private static class ProjectionAnnotationsPainter extends AnnotationPainter {
		public ProjectionAnnotationsPainter(ISourceViewer sourceViewer, IAnnotationAccess access) {
			super(sourceViewer, access);
		}
		
		/*
		 * @see org.eclipse.jface.text.source.AnnotationPainter#isRepaintReason(int)
		 */
		protected boolean isRepaintReason(int reason) {
			return true;
		}
		
		/*
		 * @see org.eclipse.jface.text.source.AnnotationPainter#findAnnotationModel(org.eclipse.jface.text.source.ISourceViewer)
		 */
		protected IAnnotationModel findAnnotationModel(ISourceViewer sourceViewer) {
			if (sourceViewer instanceof ProjectionViewer) {
				ProjectionViewer projectionViewer= (ProjectionViewer) sourceViewer;
				return projectionViewer.getProjectionAnnotationModel();
			}
			return null;
		}
	}
	
	private static class ProjectionDrawingStrategy implements AnnotationPainter.IDrawingStrategy {
		/*
		 * @see org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy#draw(org.eclipse.swt.graphics.GC, org.eclipse.swt.custom.StyledText, int, int, org.eclipse.swt.graphics.Color)
		 */
		public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
			if (gc != null && annotation instanceof ProjectionAnnotation) {
				ProjectionAnnotation projectionAnnotation= (ProjectionAnnotation) annotation;
				if (projectionAnnotation.isFolded()) {
					
					StyledTextContent content= textWidget.getContent();
					int line= content.getLineAtOffset(offset);
					int lineStart= content.getOffsetAtLine(line);
					String text= content.getLine(line);
					int lineLength= text == null ? 0 : text.length();
					int lineEnd= lineStart + lineLength;
					Point p= textWidget.getLocationAtOffset(lineEnd);
					
					Color c= gc.getForeground();
					gc.setForeground(color);
					
					FontMetrics metrics= gc.getFontMetrics();
					int lineHeight= metrics.getHeight();
					int verticalMargin= lineHeight/10;
					int height= lineHeight - 2*verticalMargin;
					int width= metrics.getAverageCharWidth();
					gc.drawRectangle(p.x, p.y + verticalMargin, width, height);
					int third= width/3;
					int dotsVertical= p.y + metrics.getLeading() + metrics.getAscent();
					gc.drawPoint(p.x + third, dotsVertical);
					gc.drawPoint(p.x + 2*third, dotsVertical);
					
					gc.setForeground(c);
				}
			}
		}
	}
	
	private final static Object PROJECTION= new Object();
	
	/**
	 * Enable projection for the given viewer
	 * 
	 * @param viewer the viewer
	 * @param annotationAccess the annotation access
	 * @param sharedTextColors the shared text colors
	 */
	public static void enableProjection(ISourceViewer viewer, IAnnotationAccess annotationAccess, ISharedTextColors sharedTextColors) {
		
		if (viewer instanceof ProjectionViewer) {
			ProjectionViewer projectionViewer= (ProjectionViewer) viewer;
			
			AnnotationPainter painter= new ProjectionAnnotationsPainter(projectionViewer, annotationAccess);
			painter.addDrawingStrategy(PROJECTION, new ProjectionDrawingStrategy());
			painter.addAnnotationType(ProjectionAnnotation.TYPE, PROJECTION);
			painter.setAnnotationTypeColor(ProjectionAnnotation.TYPE, sharedTextColors.getColor(new RGB(0, 0, 255)));
			projectionViewer.addPainter(painter);
			
			ProjectionRulerColumn column= new ProjectionRulerColumn(projectionViewer.getProjectionAnnotationModel(), 9, annotationAccess);
			column.addAnnotationType(ProjectionAnnotation.TYPE);
			projectionViewer.addVerticalRulerColumn(column);
		}
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public static Object getAdapter(ISourceViewer viewer, Class required) {
		if (IProjectionAnnotationModel.class.equals(required)) {
			if (viewer instanceof ProjectionViewer) {
				ProjectionViewer projectionViewer= (ProjectionViewer) viewer;
				return projectionViewer.getProjectionAnnotationModel();
			}
		}
		return null;
	}
}
