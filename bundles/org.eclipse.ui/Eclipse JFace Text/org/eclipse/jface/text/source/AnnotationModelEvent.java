package org.eclipse.jface.text.source;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Specification of changes applied to annotation models. 
 * The event carries the changed annotation model. 
 * Work in progress. Will also contain added, removed, and modified annotations.
 *
 * @see IAnnotationModel
 */
public class AnnotationModelEvent {
	
	/** The model this event refers to. */
	IAnnotationModel fAnnotationModel;
	
	/**
	 * Creates a new annotation model event for the given model.
	 */
	public AnnotationModelEvent(IAnnotationModel model) {
		fAnnotationModel= model;
	}
	
	/**
	 * Returns the model this event refers to.
	 */
	public IAnnotationModel getAnnotationModel() {
		return fAnnotationModel;
	}
}
