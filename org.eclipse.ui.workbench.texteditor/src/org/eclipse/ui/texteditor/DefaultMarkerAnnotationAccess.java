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
package org.eclipse.ui.texteditor;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;


/**
 * @since 2.1
 */
public class DefaultMarkerAnnotationAccess implements IAnnotationAccess {
	
	/** Constant for the unknown marker type */
	public final static String UNKNOWN= TextEditorPlugin.PLUGIN_ID + ".unknown";  //$NON-NLS-1$
	
	/** The marker annotation preferences */
	protected MarkerAnnotationPreferences fMarkerAnnotationPreferences;
	
	/**
	 * Returns a new default marker annotation access with the given preferences.
	 * 
	 * @param markerAnnotationPreferences
	 */
	public DefaultMarkerAnnotationAccess(MarkerAnnotationPreferences markerAnnotationPreferences) {
		fMarkerAnnotationPreferences= markerAnnotationPreferences;
	}

	/**
	 * Returns the annotation preference for the given marker.
	 * 
	 * @param marker
	 * @return the annotation preference or <code>null</code> if none
	 */	
	private AnnotationPreference getAnnotationPreference(IMarker marker) {
		
		try {
			
			int severity= marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);

			Iterator e= fMarkerAnnotationPreferences.getAnnotationPreferences().iterator();
			while (e.hasNext()) {
				AnnotationPreference info= (AnnotationPreference) e.next();
				if (marker.isSubtypeOf(info.getMarkerType()) && severity == info.getSeverity())
					return info;
			}
			
		} catch (CoreException x) {
		}
		
		return null;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccess#getType(org.eclipse.jface.text.source.Annotation)
	 */
	public Object getType(Annotation annotation) {
		if (annotation instanceof MarkerAnnotation) {
			MarkerAnnotation markerAnnotation= (MarkerAnnotation) annotation;
			IMarker marker= markerAnnotation.getMarker();
			if (marker != null && marker.exists()) {
				AnnotationPreference preference= getAnnotationPreference(marker);
				if (preference != null)
					return preference.getAnnotationType();
			}
		}
		return UNKNOWN;
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
