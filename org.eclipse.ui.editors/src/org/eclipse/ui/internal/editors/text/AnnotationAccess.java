/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.internal.editors.text;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;

import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * AnnotationAccess.java
 */
public class AnnotationAccess implements IAnnotationAccess {

	public AnnotationAccess() {
		super();
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccess#getType(org.eclipse.jface.text.source.Annotation)
	 */
	public Object getType(Annotation annotation) {
		AnnotationType result= AnnotationType.UNKNOWN;
		if (annotation instanceof MarkerAnnotation) {
			IMarker marker= ((MarkerAnnotation) annotation).getMarker();
			if (marker != null && marker.exists()) {
				try {
					if (marker.isSubtypeOf(IMarker.BOOKMARK)) {
						result= AnnotationType.BOOKMARK;
					} else if (marker.isSubtypeOf(IMarker.TASK)) {
						result= AnnotationType.TASK;
					} else if (marker.isSubtypeOf(IMarker.PROBLEM)) {
						int severity= marker.getAttribute(IMarker.SEVERITY, -1);
						switch (severity) {
							case IMarker.SEVERITY_ERROR:
								result= AnnotationType.ERROR;
								break;
							case IMarker.SEVERITY_WARNING:
								result= AnnotationType.WARNING;
								break;
							case IMarker.SEVERITY_INFO:
								result= AnnotationType.INFO;
								break;
						}
//					} else if (marker.isSubtypeOf(SearchUI.SEARCH_MARKER)) {
//						result= AnnotationType.SEARCH;
					}
				} catch (CoreException e) {
				}
			}
		}
		return result;
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
		return false;
	}

}
