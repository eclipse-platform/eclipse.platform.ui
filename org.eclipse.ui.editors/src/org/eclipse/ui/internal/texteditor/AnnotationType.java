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
package org.eclipse.ui.internal.texteditor;

import org.eclipse.ui.texteditor.AnnotationPreference;

/**
 * Represents an annotation type.
 *
 * @since 3.0
 */
public final class AnnotationType {
	private String fType;
	private String[] fSuperTypes;
	private AnnotationPreference fPreference;
	
	public AnnotationType(String type, String[] superTypes) {
		fType= type;
		fSuperTypes= superTypes;
	}
	
	public String getType() {
		return fType;
	}
	
	public String[] getSuperTypes() {
		return fSuperTypes;
	}
	
	/**
	 * For internal use only.
	 * 
	 * @return the annotation preference
	 */
	public AnnotationPreference getPreference() {
		return fPreference;
	}
	
	/**
	 * For internal use only.
	 * 
	 * @param preference the annotation preference
	 */
	public void setAnnotationPreference(AnnotationPreference preference) {
		fPreference= preference;
	}
	
	public boolean isSubtype(String superType) {
		if (fSuperTypes == null || superType == null)
			return false;
		
		if (superType.equals(fType))
			return true;
		
		for (int i= fSuperTypes.length -1; i > -1; i--) {
			if (superType.equals(fSuperTypes[i]))
				return true;
		}
		
		return false;
	}
}
