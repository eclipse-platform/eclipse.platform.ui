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

import java.util.HashSet;
import java.util.Set;

/**
 * Specification of changes applied to annotation models. 
 * The event carries the changed annotation model
 * as well as added, removed, and modified annotations.
 *
 * @see IAnnotationModel
 * @since 2.0
 */
public class AnnotationModelEvent {
	
	/** The model this event refers to. For internal use only. */
	IAnnotationModel fAnnotationModel;
	/**
	 * The added annotations.
	 * @since 3.0
	 */
	private Set fAddedAnnotations= new HashSet();
	/**
	 * The removed annotations.
	 * @since 3.0
	 */
	private Set fRemovedAnnotations= new HashSet();
	/** 
	 * The changed annotations.
	 * @since 3.0 
	 */
	private Set fChangedAnnotations= new HashSet();
	
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
	
	/**
	 * Adds the given annotation to the set of annotations that are reported as
	 * being added from the model.
	 * 
	 * @param annotation the added annotation
	 * @since 3.0
	 */
	public void annotationAdded(Annotation annotation) {
		fAddedAnnotations.add(annotation);
	}
	
	/**
	 * Returns the added annotations.
	 * 
	 * @return the added annotations
	 * @since 3.0
	 */
	public Annotation[] getAddedAnnotations() {
		int size= fAddedAnnotations.size();
		Annotation[] added= new Annotation[size];
		fAddedAnnotations.toArray(added);
		return added;
	}
	
	/**
	 * Adds the given annotation to the set of annotations that are reported as
	 * being removed from the model.
	 * 
	 * @param annotation the removed annotation
	 * @since 3.0
	 */
	public void annotationRemoved(Annotation annotation) {
		fRemovedAnnotations.add(annotation);
	}
	
	/**
	 * Returns the removed annotations.
	 * 
	 * @return the removed annotations
	 * @since 3.0
	 */
	public Annotation[] getRemovedAnnotations() {
		int size= fRemovedAnnotations.size();
		Annotation[] removed= new Annotation[size];
		fRemovedAnnotations.toArray(removed);
		return removed;
	}
	
	/**
	 * Adds the given annotation to the set of annotations that are reported as
	 * being changed from the model.
	 * 
	 * @param annotation the changed annotation
	 * @since 3.0
	 */
	public void annotationChanged(Annotation annotation) {
		fChangedAnnotations.add(annotation);
	}
	
	/**
	 * Returns the changed annotations.
	 * 
	 * @return the changed annotations
	 * @since 3.0
	 */
	public Annotation[] getChangedAnnotations() {
		int size= fChangedAnnotations.size();
		Annotation[] changed= new Annotation[size];
		fChangedAnnotations.toArray(changed);
		return changed;
	}
	
	/**
	 * Returns whether this annotation model event is empty or not.
	 * 
	 * @return <code>true</code> if this event is empty
	 * @since 3.0
	 */
	public boolean isEmpty() {
		return fAddedAnnotations.isEmpty() && fRemovedAnnotations.isEmpty() && fChangedAnnotations.isEmpty();
	}
}
