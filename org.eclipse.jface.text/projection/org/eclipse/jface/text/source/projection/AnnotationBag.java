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
package org.eclipse.jface.text.source.projection;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.text.source.Annotation;

/**
 * A bag of annotations.
 * 
 * @since 3.0
 */
public class AnnotationBag extends Annotation {
	
	private Set fAnnotations;
	
	/** Creates a new annotation bag.
	 * 
	 * @param type the annotation type
	 */
	public AnnotationBag(String type) {
		super(type, false, null);
	}

	public void add(Annotation annotation) {
		if (fAnnotations == null) 
			fAnnotations= new HashSet(2);
		fAnnotations.add(annotation);
	}
	
	public void remove(Annotation annotation) {
		if (fAnnotations != null) {
			fAnnotations.remove(annotation);
			if (fAnnotations.isEmpty())
				fAnnotations= null;
		}
	}
	
	public boolean isEmpty() {
		return fAnnotations == null;
	}
}
