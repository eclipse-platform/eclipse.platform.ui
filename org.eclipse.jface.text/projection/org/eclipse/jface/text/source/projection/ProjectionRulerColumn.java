/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source.projection;

import java.util.Iterator;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;


/**
 * A ruler column for controlling the behavior of a
 * {@link org.eclipse.jface.text.source.projection.ProjectionViewer}.
 *
 * @since 3.0
 */
class ProjectionRulerColumn extends AnnotationRulerColumn {

	private ProjectionAnnotation fCurrentAnnotation;

	/**
	 * Line number recorded on mouse down.
	 * @since 3.5
	 */
	private int fMouseDownLine;

	/**
	 * Creates a new projection ruler column.
	 *
	 * @param model the column's annotation model
	 * @param width the width in pixels
	 * @param annotationAccess the annotation access
	 */
	public ProjectionRulerColumn(IAnnotationModel model, int width, IAnnotationAccess annotationAccess) {
		super(model, width, annotationAccess);
	}

	/**
	 * Creates a new projection ruler column.
	 *
	 * @param width the width in pixels
	 * @param annotationAccess the annotation access
	 */
	public ProjectionRulerColumn(int width, IAnnotationAccess annotationAccess) {
		super(width, annotationAccess);
	}

	/*
	 * @see org.eclipse.jface.text.source.AnnotationRulerColumn#mouseClicked(int)
	 */
	protected void mouseClicked(int line) {
		clearCurrentAnnotation();
		if (fMouseDownLine != line)
			return;
		ProjectionAnnotation annotation= findAnnotation(line, true);
		if (annotation != null) {
			ProjectionAnnotationModel model= (ProjectionAnnotationModel) getModel();
			model.toggleExpansionState(annotation);
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.AnnotationRulerColumn#mouseDown(int)
	 * @since 3.5
	 */
	protected void mouseDown(int rulerLine) {
		fMouseDownLine= rulerLine;
	}

	/*
	 * @see org.eclipse.jface.text.source.AnnotationRulerColumn#mouseDoubleClicked(int)
	 * @since 3.5
	 */
	protected void mouseDoubleClicked(int rulerLine) {
		if (findAnnotation(rulerLine, true) != null)
			return;

		ProjectionAnnotation annotation= findAnnotation(rulerLine, false);
		if (annotation != null) {
			ProjectionAnnotationModel model= (ProjectionAnnotationModel)getModel();
			model.toggleExpansionState(annotation);
		}
	}

	/**
	 * Returns the projection annotation of the column's annotation
	 * model that contains the given line.
	 *
	 * @param line the line
	 * @param exact <code>true</code> if the annotation range must match exactly
	 * @return the projection annotation containing the given line
	 */
	private ProjectionAnnotation findAnnotation(int line, boolean exact) {

		ProjectionAnnotation previousAnnotation= null;

		IAnnotationModel model= getModel();
		if (model != null) {
			IDocument document= getCachedTextViewer().getDocument();

			int previousDistance= Integer.MAX_VALUE;

			Iterator e= model.getAnnotationIterator();
			while (e.hasNext()) {
				Object next= e.next();
				if (next instanceof ProjectionAnnotation) {
					ProjectionAnnotation annotation= (ProjectionAnnotation) next;
					Position p= model.getPosition(annotation);
					if (p == null)
						continue;

					int distance= getDistance(annotation, p, document, line);
					if (distance == -1)
						continue;

					if (!exact) {
						if (distance < previousDistance) {
							previousAnnotation= annotation;
							previousDistance= distance;
						}
					} else if (distance == 0) {
						previousAnnotation= annotation;
					}
				}
			}
		}

		return previousAnnotation;
	}

	/**
	 * Returns the distance of the given line to the start line of the given position in the given document. The distance is
	 * <code>-1</code> when the line is not included in the given position.
	 *
	 * @param annotation the annotation
	 * @param position the position
	 * @param document the document
	 * @param line the line
	 * @return <code>-1</code> if line is not contained, a position number otherwise
	 */
	private int getDistance(ProjectionAnnotation annotation, Position position, IDocument document, int line) {
		if (position.getOffset() > -1 && position.getLength() > -1) {
			try {
				int startLine= document.getLineOfOffset(position.getOffset());
				int endLine= document.getLineOfOffset(position.getOffset() + position.getLength());
				if (startLine <= line && line < endLine) {
					if (annotation.isCollapsed()) {
						int captionOffset;
						if (position instanceof IProjectionPosition)
							captionOffset= ((IProjectionPosition) position).computeCaptionOffset(document);
						else
							captionOffset= 0;

						int captionLine= document.getLineOfOffset(position.getOffset() + captionOffset);
						if (startLine <= captionLine && captionLine < endLine)
							return Math.abs(line - captionLine);
					}
					return line - startLine;
				}
			} catch (BadLocationException x) {
			}
		}
		return -1;
	}

	private boolean clearCurrentAnnotation() {
		if (fCurrentAnnotation != null) {
			fCurrentAnnotation.setRangeIndication(false);
			fCurrentAnnotation= null;
			return true;
		}
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerColumn#createControl(org.eclipse.jface.text.source.CompositeRuler, org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
		Control control= super.createControl(parentRuler, parentControl);

		// set background
		Color background= getCachedTextViewer().getTextWidget().getBackground();
		control.setBackground(background);

		// install hover listener
		control.addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseExit(MouseEvent e) {
				if (clearCurrentAnnotation())
					redraw();
			}
		});

		// install mouse move listener
		control.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				boolean redraw= false;
				ProjectionAnnotation annotation= findAnnotation(toDocumentLineNumber(e.y), false);
				if (annotation != fCurrentAnnotation) {
					if (fCurrentAnnotation != null) {
						fCurrentAnnotation.setRangeIndication(false);
						redraw= true;
					}
					fCurrentAnnotation= annotation;
					if (fCurrentAnnotation != null && !fCurrentAnnotation.isCollapsed()) {
						fCurrentAnnotation.setRangeIndication(true);
						redraw= true;
					}
				}
				if (redraw)
					redraw();
			}
		});
		return control;
	}

	/*
	 * @see org.eclipse.jface.text.source.AnnotationRulerColumn#setModel(org.eclipse.jface.text.source.IAnnotationModel)
	 */
	public void setModel(IAnnotationModel model) {
		if (model instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension extension= (IAnnotationModelExtension) model;
			model= extension.getAnnotationModel(ProjectionSupport.PROJECTION);
		}
		super.setModel(model);
	}

	/*
	 * @see org.eclipse.jface.text.source.AnnotationRulerColumn#isPropagatingMouseListener()
	 */
	protected boolean isPropagatingMouseListener() {
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.source.AnnotationRulerColumn#hasAnnotation(int)
	 */
	protected boolean hasAnnotation(int lineNumber) {
		return findAnnotation(lineNumber, true) != null;
	}
}
