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
package org.eclipse.ant.internal.ui.editor.text;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;

/**
 * AnnotationAccess.java
 */
public class AnnotationAccess extends DefaultMarkerAnnotationAccess {

	/**
	 * @param markerAnnotationPreferences
	 */
	public AnnotationAccess(MarkerAnnotationPreferences markerAnnotationPreferences) {
		super(markerAnnotationPreferences);
	}

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
		
		return super.getType(annotation);
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
		return super.isTemporary(annotation);
	}
}
