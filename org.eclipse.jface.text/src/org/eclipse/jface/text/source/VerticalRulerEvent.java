/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.swt.widgets.Event;


/**
 * An event sent to {@link org.eclipse.jface.text.source.IVerticalRulerListener} instances when annotation
 * related event occurs on the vertical ruler.
 *
 * @since 3.0
 */
public class VerticalRulerEvent {

	private Annotation fAnnotation;
	private Event fEvent;

	/**
	 * Creates a new event.
	 *
	 * @param annotation the annotation concerned, or <code>null</code>
	 */
	public VerticalRulerEvent(Annotation annotation) {
		fAnnotation= annotation;
	}

	/**
	 * Creates a new event.
	 *
	 * @param annotation the annotation concerned, or <code>null</code>
	 * @param event the SWT event that triggered this vertical ruler event, or <code>null</code>
	 * @since 3.8
	 */
	public VerticalRulerEvent(Annotation annotation, Event event) {
		fAnnotation= annotation;
		fEvent= event;
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
	
	/**
	 * @return the SWT event that triggered this vertical ruler event, or <code>null</code>. 
	 * @since 3.8
	 */
	public Event getEvent() {
		return fEvent;
	}
}
