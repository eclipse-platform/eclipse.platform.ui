/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * Supports the configuration of projection capabilities a {@link org.eclipse.jface.text.source.projection.ProjectionViewer}.
 * <p>
 * This class is not intended to be subclassed. Clients are supposed to configure and use it as is.</p>
 *
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ProjectionSupport {

	/**
	 * Key of the projection annotation model inside the visual annotation
	 * model. Also internally used as key for the projection drawing strategy.
	 */
	public final static Object PROJECTION= new Object();

	private static class ProjectionAnnotationsPainter extends AnnotationPainter {

		/**
		 * Creates a new painter indicating the location of collapsed regions.
		 *
		 * @param sourceViewer the source viewer for the painter
		 * @param access the annotation access
		 */
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
						int baseline= textWidget.getBaseline(offset);
						// descent: number of pixels that the box extends over baseline
						int descent= Math.min(2, textWidget.getLineHeight(offset) - baseline);
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
	private IInformationControlCreator fInformationPresenterControlCreator;
	private ProjectionListener fProjectionListener;
	private ProjectionAnnotationsPainter fPainter;
	private ProjectionRulerColumn fColumn;
	/**
	 * @since 3.1
	 */
	private AnnotationPainter.IDrawingStrategy fDrawingStrategy;

	/**
	 * Creates new projection support for the given projection viewer. Initially,
	 * no annotation types are summarized. A default hover control creator and a
	 * default drawing strategy are used.
	 *
	 * @param viewer the projection viewer
	 * @param annotationAccess the annotation access
	 * @param sharedTextColors the shared text colors to use
	 */
	public ProjectionSupport(ProjectionViewer viewer, IAnnotationAccess annotationAccess, ISharedTextColors sharedTextColors) {
		fViewer= viewer;
		fAnnotationAccess= annotationAccess;
		fSharedTextColors= sharedTextColors;
	}

	/**
	 * Marks the given annotation type to be considered when creating summaries for
	 * collapsed regions of the projection viewer.
	 * <p>
	 * A summary is an annotation that gets created out of all annotations with a
	 * type that has been registered through this method and that are inside the
	 * folded region.
	 * </p>
	 *
	 * @param annotationType the annotation type to consider
	 */
	public void addSummarizableAnnotationType(String annotationType) {
		if (fSummarizableTypes == null) {
			fSummarizableTypes= new ArrayList();
			fSummarizableTypes.add(annotationType);
		} else if (!fSummarizableTypes.contains(annotationType))
			fSummarizableTypes.add(annotationType);
	}

	/**
	 * Marks the given annotation type to be ignored when creating summaries for
	 * collapsed regions of the projection viewer. This method has only an effect
	 * when <code>addSummarizableAnnotationType</code> has been called before for
	 * the give annotation type.
	 * <p>
	 * A summary is an annotation that gets created out of all annotations with a
	 * type that has been registered through this method and that are inside the
	 * folded region.
	 * </p>
	 *
	 * @param annotationType the annotation type to remove
	 */
	public void removeSummarizableAnnotationType(String annotationType) {
		if (fSummarizableTypes != null) {
			fSummarizableTypes.remove(annotationType);
			if (fSummarizableTypes.size() == 0)
				fSummarizableTypes= null;
		}
	}

	/**
	 * Sets the hover control creator that is used for the annotation hovers
	 * that are shown in the projection viewer's projection ruler column.
	 *
	 * @param creator the hover control creator
	 */
	public void setHoverControlCreator(IInformationControlCreator creator) {
		fInformationControlCreator= creator;
	}

	/**
	 * Sets the information presenter control creator that is used for the annotation
	 * hovers that are shown in the projection viewer's projection ruler column.
	 *
	 * @param creator the information presenter control creator
	 * @since 3.3
	 */
	public void setInformationPresenterControlCreator(IInformationControlCreator creator) {
		fInformationPresenterControlCreator= creator;
	}

	/**
	 * Sets the drawing strategy that the projection support's annotation
	 * painter uses to draw the indication of collapsed regions onto the
	 * projection viewer's text widget. When <code>null</code> is passed in,
	 * the drawing strategy is reset to the default. In order to avoid any
	 * representation use {@link org.eclipse.jface.text.source.AnnotationPainter.NullStrategy}.
	 *
	 * @param strategy the drawing strategy or <code>null</code> to reset the
	 *            strategy to the default
	 * @since 3.1
	 */
	public void setAnnotationPainterDrawingStrategy(AnnotationPainter.IDrawingStrategy strategy) {
		fDrawingStrategy= strategy;
	}

	/**
	 * Returns the drawing strategy to be used by the support's annotation painter.
	 *
	 * @return the drawing strategy to be used by the support's annotation painter
	 * @since 3.1
	 */
	private AnnotationPainter.IDrawingStrategy getDrawingStrategy() {
		if (fDrawingStrategy == null)
			fDrawingStrategy= new ProjectionDrawingStrategy();
		return fDrawingStrategy;
	}

	/**
	 * Installs this projection support on its viewer.
	 */
	public void install() {
		fViewer.setProjectionSummary(createProjectionSummary());

		fProjectionListener= new ProjectionListener();
		fViewer.addProjectionListener(fProjectionListener);
	}

	/**
	 * Disposes this projection support.
	 */
	public void dispose() {
		if (fProjectionListener != null) {
			fViewer.removeProjectionListener(fProjectionListener);
			fProjectionListener= null;
		}
	}

	/**
	 * Enables projection mode. If not yet done, installs the projection ruler
	 * column in the viewer's vertical ruler and installs a painter that
	 * indicate the locations of collapsed regions.
	 *
	 */
	protected void doEnableProjection() {

		if (fPainter == null) {
			fPainter= new ProjectionAnnotationsPainter(fViewer, fAnnotationAccess);
			fPainter.addDrawingStrategy(PROJECTION, getDrawingStrategy());
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

	/**
	 * Removes the projection ruler column and the painter from the projection
	 * viewer.
	 */
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
		hover.setInformationPresenterControlCreator(fInformationPresenterControlCreator);
		return hover;
	}

	/**
	 * Implements the contract of {@link org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)}
	 * by forwarding the adapter requests to the given viewer.
	 *
	 * @param viewer the viewer
	 * @param required the required class of the adapter
	 * @return the adapter or <code>null</code>
	 *
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
