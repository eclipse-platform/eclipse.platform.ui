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

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;

/**
 * AnnotationAccess.java
 */
public class AnnotationAccess implements IAnnotationAccess {

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccess#getType(org.eclipse.jface.text.source.Annotation)
	 */
	public Object getType(Annotation annotation) {
		if (annotation instanceof IXMLAnnotation) {
			IXMLAnnotation xmlAnnotation= (IXMLAnnotation) annotation;
			if (xmlAnnotation.isRelevant()) {
				return xmlAnnotation.getAnnotationType();
			}
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccess#isMultiLine(org.eclipse.jface.text.source.Annotation)
	 */
	public boolean isMultiLine(Annotation annotation) {
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccess#isTemporary(org.eclipse.jface.text.source.Annotation)
	 */
	public boolean isTemporary(Annotation annotation) {
		if (annotation instanceof IXMLAnnotation) {
			IXMLAnnotation xmlAnnotation= (IXMLAnnotation) annotation;
			if (xmlAnnotation.isRelevant()) {
				return xmlAnnotation.isTemporary();
			}
		}
		return false;
	}
}
