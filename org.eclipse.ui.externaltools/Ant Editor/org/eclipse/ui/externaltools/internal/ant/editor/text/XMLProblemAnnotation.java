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
package org.eclipse.ui.externaltools.internal.ant.editor.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.externaltools.internal.ant.editor.outline.IProblem;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * Annotation representating an <code>IProblem</code>.
 */
public class XMLProblemAnnotation extends Annotation implements IXMLAnnotation {
	
	private List fOverlaids;
	private IProblem fProblem;
	private AnnotationType fType;
	
	
	public XMLProblemAnnotation(IProblem problem) {
		
		fProblem= problem;
		setLayer(MarkerAnnotation.PROBLEM_LAYER + 1);
		
		if (fProblem.isError())
			fType= AnnotationType.ERROR;
		else if (fProblem.isWarning())
			fType= AnnotationType.WARNING;
		else
			fType= AnnotationType.INFO;			
	}
	
	/*
	 * @see Annotation#paint
	 */
	public void paint(GC gc, Canvas canvas, Rectangle r) {
	}
	
	/*
	 * @see IXMLAnnotation#getImage(Display)
	 */
	public Image getImage(Display display) {
		return null;
	}
	
	/*
	 * @see IXMLAnnotation#getMessage()
	 */
	public String getMessage() {
		return fProblem.getMessage();
	}

	/*
	 * @see IXMLAnnotation#isTemporary()
	 */
	public boolean isTemporary() {
		return true;
	}
	
	/*
	 * @see IXMLAnnotation#isProblem()
	 */
	public boolean isProblem() {
		return  fType == AnnotationType.WARNING || fType == AnnotationType.ERROR;
	}
	
	/*
	 * @see IXMLAnnotation#isRelevant()
	 */
	public boolean isRelevant() {
		return true;
	}
	
	/*
	 * @see IXMLAnnotation#hasOverlay()
	 */
	public boolean hasOverlay() {
		return false;
	}
	
	/*
	 * @see IXMLAnnotation#addOverlaid(IXMLAnnotation)
	 */
	public void addOverlaid(IXMLAnnotation annotation) {
		if (fOverlaids == null)
			fOverlaids= new ArrayList(1);
		fOverlaids.add(annotation);
	}

	/*
	 * @see IXMLAnnotation#removeOverlaid(IXMLAnnotation)
	 */
	public void removeOverlaid(IXMLAnnotation annotation) {
		if (fOverlaids != null) {
			fOverlaids.remove(annotation);
			if (fOverlaids.size() == 0)
				fOverlaids= null;
		}
	}
	
	/*
	 * @see IXMLAnnotation#getOverlaidIterator()
	 */
	public Iterator getOverlaidIterator() {
		if (fOverlaids != null)
			return fOverlaids.iterator();
		return null;
	}
	
	public AnnotationType getAnnotationType() {
		return fType;
	}
}
