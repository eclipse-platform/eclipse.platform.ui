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


import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;


/**
 * A ruler column for controlling the behavior of a
 * <code>ProjectionSourceViewer</code>.
 * <p>
 * Internal class. Do not use. Public for testing purposes only.
 * 
 * @since 3.0
 */
public class ProjectionRulerColumn extends AnnotationRulerColumn {
	
	private ProjectionAnnotation fCurrentAnnotation;

	/**
	 * Creates a new outliner ruler column.
	 * 
	 * @param model the column's annotation model
	 * @param width the width in pixels
	 */
	public ProjectionRulerColumn(IAnnotationModel model, int width, IAnnotationAccess annotationAccess) {
		super(model, width, annotationAccess);
	}
	
	/*
	 * @see org.eclipse.jface.text.source.AnnotationRulerColumn#mouseDoubleClicked(int)
	 */
	protected void mouseDoubleClicked(int line) {
		ProjectionAnnotation annotation= findAnnotation(line);
		if (annotation != null)
			annotation.run(getCachedTextViewer());
	}
	
	/**
	 * Returns the projection annotation of the column's annotation
	 * model that contains the given line.
	 * 
	 * @param line the line
	 * @return the projection annotation containing the given line
	 */
	private ProjectionAnnotation findAnnotation(int line) {
		IAnnotationModel model= getModel();
		if (model != null) {
			Iterator e= model.getAnnotationIterator();
			while (e.hasNext()) {
				Object next= e.next();
				if (next instanceof ProjectionAnnotation) {
					ProjectionAnnotation annotation= (ProjectionAnnotation) next;
					Position p= model.getPosition(annotation);
					if (contains(p, line))
						return annotation;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns whether the given position contains the given line.
	 * 
	 * @param p the position
	 * @param line the line
	 * @return <code>true</code> if the given position contains the given line, <code>false</code> otherwise
	 */
	private boolean contains(Position p, int line) {
		
		IDocument document= getCachedTextViewer().getDocument();
		
		try {
			
			int startLine= document.getLineOfOffset(p.getOffset());
			if (line < startLine)
				return false;
			if (line == startLine)
				return true;
				
			int endLine= document.getLineOfOffset(p.getOffset() + Math.max(p.getLength() -1, 0));
			return  (startLine < line && line <= endLine);
		
		} catch (BadLocationException x) {
		}
		
		return false;
	}
	
	protected boolean clearCurrentAnnotation() {
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
		Display display= parentControl.getDisplay();
		Color background= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		control.setBackground(background);
		// install hover listener
		control.addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseHover(MouseEvent e) {
				boolean redraw= clearCurrentAnnotation();
				ProjectionAnnotation annotation= findAnnotation(toDocumentLineNumber(e.y));
				if (annotation != null) {
					annotation.setRangeIndication(true);
					fCurrentAnnotation= annotation;
					redraw= true;
				}
				if (redraw)
					redraw();

			}
			public void mouseExit(MouseEvent e) {
				if (clearCurrentAnnotation())
					redraw();
			}
		});
		// install mouse move listener
		control.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (clearCurrentAnnotation())
					redraw();
			}
		});
		return control;
	}
}
