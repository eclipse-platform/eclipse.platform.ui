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
package org.eclipse.ant.ui.internal.editor.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ant.ui.internal.editor.outline.IProblem;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * Annotation representating an <code>IProblem</code>.
 */
public class XMLProblemAnnotation extends Annotation implements IXMLAnnotation {
	
	private List fOverlaids;
	private IProblem fProblem;
	private String fType;
	
	
	public XMLProblemAnnotation(IProblem problem) {
		
		fProblem= problem;
		setLayer(MarkerAnnotation.PROBLEM_LAYER + 1);
		
		if (fProblem.isError()) {
			fType= ERROR_ANNOTATION_TYPE;
		} else if (fProblem.isWarning()) {
			fType= WARNING_ANNOTATION_TYPE;
		} else {
			fType= INFO_ANNOTATION_TYPE;
		}	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.Annotation#paint(org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
	 */
	public void paint(GC gc, Canvas canvas, Rectangle r) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.editor.text.IXMLAnnotation#getImage(org.eclipse.swt.widgets.Display)
	 */
	public Image getImage(Display display) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.editor.text.IXMLAnnotation#getMessage()
	 */
	public String getMessage() {
		return fProblem.getMessage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.editor.text.IXMLAnnotation#isTemporary()
	 */
	public boolean isTemporary() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.editor.text.IXMLAnnotation#isProblem()
	 */
	public boolean isProblem() {
		return WARNING_ANNOTATION_TYPE.equals(fType) || ERROR_ANNOTATION_TYPE.equals(fType);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.editor.text.IXMLAnnotation#isRelevant()
	 */
	public boolean isRelevant() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.editor.text.IXMLAnnotation#hasOverlay()
	 */
	public boolean hasOverlay() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.editor.text.IXMLAnnotation#addOverlaid(org.eclipse.ant.ui.internal.editor.text.IXMLAnnotation)
	 */
	public void addOverlaid(IXMLAnnotation annotation) {
		if (fOverlaids == null) {
			fOverlaids= new ArrayList(1);
		}
		fOverlaids.add(annotation);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.editor.text.IXMLAnnotation#removeOverlaid(org.eclipse.ant.ui.internal.editor.text.IXMLAnnotation)
	 */
	public void removeOverlaid(IXMLAnnotation annotation) {
		if (fOverlaids != null) {
			fOverlaids.remove(annotation);
			if (fOverlaids.size() == 0) {
				fOverlaids= null;
			}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.editor.text.IXMLAnnotation#getOverlaidIterator()
	 */
	public Iterator getOverlaidIterator() {
		if (fOverlaids != null) {
			return fOverlaids.iterator();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.editor.text.IXMLAnnotation#getAnnotationType()
	 */
	public String getAnnotationType() {
		return fType;
	}
}
