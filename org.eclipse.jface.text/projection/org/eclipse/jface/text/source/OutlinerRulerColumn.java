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
 * OutlinerRulerColumn.java
 */
public class OutlinerRulerColumn extends AnnotationRulerColumn {

	/**
	 * Constructor for OutlinerRulerColumn.
	 * @param model
	 * @param width
	 */
	public OutlinerRulerColumn(IAnnotationModel model, int width) {
		super(model, width);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.ProjectionRulerColumn#mouseDoubleClicked(int)
	 */
	protected void mouseDoubleClicked(int line) {
		ProjectionAnnotation annotation= findAnnotation(line);
		if (annotation != null)
			annotation.run(getCachedTextViewer());
	}
	
	/**
	 * Method findAnnotation.
	 * @return ProjectionAnnotation
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
	 * Method contains.
	 * @param p
	 * @param line
	 * @return boolean
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
	
	/* (non-Javadoc)
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
