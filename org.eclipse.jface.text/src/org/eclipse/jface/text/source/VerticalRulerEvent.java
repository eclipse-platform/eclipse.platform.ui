/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;


/**
 * An event sent to {@link org.eclipse.jface.text.source.IVerticalRulerListener} instances when annotation
 * related event occurs on the vertical ruler.
 *
 * @since 3.0
 */
public class VerticalRulerEvent {

	private Annotation fAnnotation;

	/**
	 * Creates a new event.
	 *
	 * @param annotation the annotation concerned, or <code>null</code>
	 */
	public VerticalRulerEvent(Annotation annotation) {
		fAnnotation= annotation;
	}

	/**
	 * @return the concerned annotation or <code>null</code>
	 */
	public Annotation getSelectedAnnotation() {
		return fAnnotation;
	}

	/**
	 * @param annotation the concerned annotation, or <code>null</code>
	 */
	public void setSelectedAnnotation(Annotation annotation) {
		fAnnotation= annotation;
	}
}
