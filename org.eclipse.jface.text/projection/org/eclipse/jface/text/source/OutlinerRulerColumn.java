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


import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;


/**
 * A ruler column for controlling the behavior of a <code>ProjectionSourceViewer</code>.
 * This class is for internal use only.
 * 
 * @since 2.1
 */
public class OutlinerRulerColumn extends AnnotationRulerColumn {

	/**
	 * Creates a new outliner ruler column.
	 * 
	 * @param model the column's annotation model
	 * @param width the width in pixels
	 */
	public OutlinerRulerColumn(IAnnotationModel model, int width) {
		super(model, width);
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
			return  (startLine < line && line <= endLine)	;
		
		} catch (BadLocationException x) {
		}
		
		return false;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerColumn#createControl(org.eclipse.jface.text.source.CompositeRuler, org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
		Control control= super.createControl(parentRuler, parentControl);
		Display display= parentControl.getDisplay();
		Color background= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		control.setBackground(background);
		return control;
	}
}
