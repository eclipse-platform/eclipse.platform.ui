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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Supports the configuration of projection capabilities for projection viewers.
 * 
 * @since 3.0
 */
public class ProjectionSupport {
	
	/**
	 * Key of the projection annotation model inside the visual annotation
	 * model. Also internally used as key for the projection drawing strategy.
	 */
	public final static Object PROJECTION= new Object();

	private static class ProjectionAnnotationsPainter extends AnnotationPainter {
		public ProjectionAnnotationsPainter(ISourceViewer sourceViewer, IAnnotationAccess access) {
			super(sourceViewer, access);
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
		
		/*
		 * @see org.eclipse.jface.text.source.AnnotationPainter#skip(org.eclipse.jface.text.source.Annotation)
		 */
		protected boolean skip(Annotation annotation) {
			if (annotation instanceof ProjectionAnnotation)
				return !((ProjectionAnnotation) annotation).isCollapsed();
			
			return super.skip(annotation);
		}
	}
	
	private static class ProjectionDrawingStrategy implements AnnotationPainter.IDrawingStrategy {
		/*
		 * @see org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy#draw(org.eclipse.swt.graphics.GC, org.eclipse.swt.custom.StyledText, int, int, org.eclipse.swt.graphics.Color)
		 */
		public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
			if (annotation instanceof ProjectionAnnotation) {
				ProjectionAnnotation projectionAnnotation= (ProjectionAnnotation) annotation;
				if (projectionAnnotation.isCollapsed()) {
					
					if (gc != null) {
						
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
						
						// baseline: where the dots are drawn 
						int baseline= textWidget.getBaseline();
						// descent: number of pixels that the box extends over baseline
						int descent= Math.min(2, textWidget.getLineHeight() - baseline);
						// ascent: so much does the box stand up from baseline
						int ascent= metrics.getAscent();
						// leading: free space from line top to box upper line
						int leading= baseline - ascent;
						// height: height of the box
						int height= ascent + descent;
						
						int width= metrics.getAverageCharWidth();
						gc.drawRectangle(p.x, p.y + leading, width, height);
						int third= width/3;
						int dotsVertical= p.y + baseline - 1;
						gc.drawPoint(p.x + third, dotsVertical);
						gc.drawPoint(p.x + width - third, dotsVertical);
						
						gc.setForeground(c);
						
					} else {
						textWidget.redrawRange(offset, length, true);
					}
				}
			}
		}
	}
	
	private class ProjectionListener implements IProjectionListener {

		/*
		 * @see org.eclipse.jface.text.source.projection.IProjectionListener#projectionEnabled()
		 */
		public void projectionEnabled() {
			doEnableProjection();
		}
		
		/*
		 * @see org.eclipse.jface.text.source.projection.IProjectionListener#projectionDisabled()
		 */
		public void projectionDisabled() {
			doDisableProjection();
		}
	}
	
	private ProjectionViewer fViewer;
	private IAnnotationAccess fAnnotationAccess;
	private ISharedTextColors fSharedTextColors;
	private List fSummarizableTypes;
	private IInformationControlCreator fInformationControlCreator;
	private ProjectionListener fProjectionListener;
	private ProjectionAnnotationsPainter fPainter;
	private ProjectionRulerColumn fColumn;
	
	
	public ProjectionSupport(ProjectionViewer viewer, IAnnotationAccess annotationAccess, ISharedTextColors sharedTextColors) {
		fViewer= viewer;
		fAnnotationAccess= annotationAccess;
		fSharedTextColors= sharedTextColors;
	}
	
	public void addSummarizableAnnotationType(String annotationType) {
		if (fSummarizableTypes == null) {
			fSummarizableTypes= new ArrayList();
			fSummarizableTypes.add(annotationType);
		} else if (!fSummarizableTypes.contains(annotationType))
			fSummarizableTypes.add(annotationType);
	}
	
	public void removeSummarizableAnnotationType(String annotationType) {
		if (fSummarizableTypes != null)
			fSummarizableTypes.remove(annotationType);
		if (fSummarizableTypes.size() == 0)
			fSummarizableTypes= null;
	}
	
	public void setHoverControlCreator(IInformationControlCreator creator) {
		fInformationControlCreator= creator;
	}
		
	public void install() {
		fViewer.setProjectionSummary(createProjectionSummary());
		
		fProjectionListener= new ProjectionListener();
		fViewer.addProjectionListener(fProjectionListener);
	}
	
	public void dispose() {
		if (fProjectionListener != null) {
			fViewer.removeProjectionListener(fProjectionListener);
			fProjectionListener= null;
		}
	}
	
	protected void doEnableProjection() {
		
		if (fPainter == null) {
			fPainter= new ProjectionAnnotationsPainter(fViewer, fAnnotationAccess);
			fPainter.addDrawingStrategy(PROJECTION, new ProjectionDrawingStrategy());
			fPainter.addAnnotationType(ProjectionAnnotation.TYPE, PROJECTION);
			fPainter.setAnnotationTypeColor(ProjectionAnnotation.TYPE, fSharedTextColors.getColor(getColor()));
			fViewer.addPainter(fPainter);
		}
		
		if (fColumn == null) {
			fColumn= new ProjectionRulerColumn(9, fAnnotationAccess);
			fColumn.addAnnotationType(ProjectionAnnotation.TYPE);
			fColumn.setHover(createProjectionAnnotationHover());
			fViewer.addVerticalRulerColumn(fColumn);
		}
		
		fColumn.setModel(fViewer.getVisualAnnotationModel());
	}
	
	protected void doDisableProjection() {
		if (fPainter != null) {
			fViewer.removePainter(fPainter);
			fPainter.dispose();
			fPainter= null;
		}
		
		if (fColumn != null) {
			fViewer.removeVerticalRulerColumn(fColumn);
			fColumn= null;
		}
	}
	
	private ProjectionSummary createProjectionSummary() {
		ProjectionSummary summary= new ProjectionSummary(fViewer, fAnnotationAccess);
		if (fSummarizableTypes != null) {
			int size= fSummarizableTypes.size();
			for (int i= 0; i < size; i++)
				summary.addAnnotationType((String) fSummarizableTypes.get(i));
		}
		return summary;
	}
	
	private IAnnotationHover createProjectionAnnotationHover() {
		ProjectionAnnotationHover hover= new ProjectionAnnotationHover();
		hover.setHoverControlCreator(fInformationControlCreator);
		return hover;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(ISourceViewer viewer, Class required) {
		if (ProjectionAnnotationModel.class.equals(required)) {
			if (viewer instanceof ProjectionViewer) {
				ProjectionViewer projectionViewer= (ProjectionViewer) viewer;
				return projectionViewer.getProjectionAnnotationModel();
			}
		}
		return null;
	}
	
	private RGB getColor() {
		// TODO read out preference settings
		Color c= Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
		return c.getRGB();
	}
}
