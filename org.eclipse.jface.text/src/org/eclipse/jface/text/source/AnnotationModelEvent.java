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
	private IAnnotationModel fAnnotationModel;
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
	 * Indicates that this event does not contain detailed information.
	 * @since 3.0
	 */
	private boolean fIsWorldChange;
	
	/**
	 * Creates a new annotation model event for the given model.
	 * 
	 * @param model the model 
	 */
	public AnnotationModelEvent(IAnnotationModel model) {
		this(model, true);
	}

	/**
	 * Creates a new annotation model event for the given model.
	 * 
	 * @param model the model
	 * @param isWorldChange <code>true</code> if world change
	 * @since 3.0
	 */
	public AnnotationModelEvent(IAnnotationModel model, boolean isWorldChange) {
		fAnnotationModel= model;
		fIsWorldChange= isWorldChange;
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
	 * being added from the model. If this event is considered a world change,
	 * it is no longer so after this method has successfully finished.
	 * 
	 * @param annotation the added annotation
	 * @since 3.0
	 */
	public void annotationAdded(Annotation annotation) {
		fAddedAnnotations.add(annotation);
		fIsWorldChange= false;
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
	 * being removed from the model. If this event is considered a world
	 * change, it is no longer so after this method has successfully finished.
	 * 
	 * @param annotation the removed annotation
	 * @since 3.0
	 */
	public void annotationRemoved(Annotation annotation) {
		fRemovedAnnotations.add(annotation);
		fIsWorldChange= false;
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
	 * being changed from the model. If this event is considered a world
	 * change, it is no longer so after this method has successfully finished.
	 * 
	 * @param annotation the changed annotation
	 * @since 3.0
	 */
	public void annotationChanged(Annotation annotation) {
		fChangedAnnotations.add(annotation);
		fIsWorldChange= false;
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
	 * Returns whether this annotation model event is empty or not. If this
	 * event represents a world change, this method returns <code>false</code>
	 * although the event does not carry any added, removed, or changed
	 * annotations.
	 * 
	 * @return <code>true</code> if this event is empty
	 * @since 3.0
	 */
	public boolean isEmpty() {
		return !fIsWorldChange && fAddedAnnotations.isEmpty() && fRemovedAnnotations.isEmpty() && fChangedAnnotations.isEmpty();
	}
	
	/**
	 * Returns whether this annotation model events contains detailed
	 * information about the modifications applied to the event annotation
	 * model or whether it represents a world change, i.e. everything in the
	 * model might have changed.
	 * 
	 * @return <code>true</code> if world change, <code>false</code> otherwise
	 */
	public boolean isWorldChange() {
		return fIsWorldChange;
	}
	
	/**
	 * Marks this event as world change according to the given flag.
	 * 
	 * @param worldChange <code>true</code> if this event is a world change, <code>false</code> otherwise
	 * @since 3.0
	 */
	void markWorldChange(boolean isWorldChange) {
		fIsWorldChange= isWorldChange;
	}
}
