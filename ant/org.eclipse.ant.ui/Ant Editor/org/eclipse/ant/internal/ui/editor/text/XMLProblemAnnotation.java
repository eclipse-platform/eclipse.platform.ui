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
package org.eclipse.ant.internal.ui.editor.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.internal.ui.editor.outline.IProblem;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Annotation representating an <code>IProblem</code>.
 */
public class XMLProblemAnnotation extends Annotation implements IXMLAnnotation {
	
	private List fOverlaids;
	private IProblem fProblem;
	
	public XMLProblemAnnotation(IProblem problem) {
		
		fProblem= problem;
		
		if (fProblem.isError()) {
			setType(ERROR_ANNOTATION_TYPE);
		} else if (fProblem.isWarning()) {
			setType(WARNING_ANNOTATION_TYPE);
		} else {
			setType(INFO_ANNOTATION_TYPE);
		}	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#getImage(org.eclipse.swt.widgets.Display)
	 */
	public Image getImage(Display display) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#getMessage()
	 */
	public String getMessage() {
		return fProblem.getMessage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#isTemporary()
	 */
	public boolean isTemporary() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#isProblem()
	 */
	public boolean isProblem() {
		return WARNING_ANNOTATION_TYPE.equals(getType()) || ERROR_ANNOTATION_TYPE.equals(getType());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#isRelevant()
	 */
	public boolean isRelevant() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#hasOverlay()
	 */
	public boolean hasOverlay() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#addOverlaid(org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation)
	 */
	public void addOverlaid(IXMLAnnotation annotation) {
		if (fOverlaids == null) {
			fOverlaids= new ArrayList(1);
		}
		fOverlaids.add(annotation);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#removeOverlaid(org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation)
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
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#getOverlaidIterator()
	 */
	public Iterator getOverlaidIterator() {
		if (fOverlaids != null) {
			return fOverlaids.iterator();
		}
		return null;
	}
}