/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text.source;

/**
 * Specification of changes applied to annotation models. 
 * The event carries the changed annotation model. <p>
 * Work in progress. Intented to also contain added, removed, and modified annotations.
 *
 * @see IAnnotationModel
 * @since 2.0
 */
public class AnnotationModelEvent {
	
	/** The model this event refers to. For internal use only. */
	IAnnotationModel fAnnotationModel;
	
	/**
	 * Creates a new annotation model event for the given model.
	 * 
	 * @param model the model 
	 */
	public AnnotationModelEvent(IAnnotationModel model) {
		fAnnotationModel= model;
	}
	
	/**
	 * Returns the model this event refers to.
	 * 
	 * @return the model this events belongs to
	 */
	public IAnnotationModel getAnnotationModel() {
		return fAnnotationModel;
	}
}
