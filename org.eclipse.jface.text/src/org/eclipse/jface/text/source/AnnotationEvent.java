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


/**
 * An event sent to {@link org.eclipse.jface.text.source.IAnnotationListener} instances when annotation
 * selection etc. occurs.
 * 
 * <p>
 * TODO Note that this is work in progress and the interface is still subject to change.
 * </p>
 * 
 * @since 3.0
 */
public class AnnotationEvent {
	private Annotation fAnnotation;
	
	/**
	 * Creates a new event.
	 * 
	 * @param annotation the annotation concerned, or <code>null</code>
	 */
	public AnnotationEvent(Annotation annotation) {
		fAnnotation= annotation;
	}

	/**
	 * @return the concerned annotation or <code>null</code>
	 */
	public Annotation getAnnotation() {
		return fAnnotation;
	}
	
	/**
	 * @param annotation the concerned annotation, or <code>null</code>
	 */
	public void setAnnotation(Annotation annotation) {
		fAnnotation= annotation;
	}
}
