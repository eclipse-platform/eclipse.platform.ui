/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.Position;


/**
 * Specification of changes applied to annotation models. The event carries the
 * changed annotation model as well as added, removed, and modified annotations.
 * <p>
 * An event can be sealed. Afterwards it can not be modified. Thus, the normal
 * process is that an empty event is created, filled with the changed
 * information, and before it is sent to the listeners, the event is sealed.
 *
 * @see org.eclipse.jface.text.source.IAnnotationModel
 * @see org.eclipse.jface.text.source.IAnnotationModelListenerExtension
 * @since 2.0
 */
public class AnnotationModelEvent {

	/** The model this event refers to. */
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
	private Map fRemovedAnnotations= new HashMap();
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
	 * The modification stamp.
	 * @since 3.0
	 */
	private Object fModificationStamp;

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
		annotationRemoved(annotation, null);
	}

	/**
	 * Adds the given annotation to the set of annotations that are reported as
	 * being removed from the model. If this event is considered a world
	 * change, it is no longer so after this method has successfully finished.
	 *
	 * @param annotation the removed annotation
	 * @param position the position of the removed annotation
	 * @since 3.0
	 */
	public void annotationRemoved(Annotation annotation, Position position) {
		fRemovedAnnotations.put(annotation, position);
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
		fRemovedAnnotations.keySet().toArray(removed);
		return removed;
	}

	/**
	 * Returns the position of the removed annotation at that point in time
	 * when the annotation has been removed.
	 *
	 * @param annotation the removed annotation
	 * @return the position of the removed annotation or <code>null</code>
	 * @since 3.0
	 */
	public Position getPositionOfRemovedAnnotation(Annotation annotation) {
		return (Position) fRemovedAnnotations.get(annotation);
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
	 * model or whether it represents a world change. I.e. everything in the
	 * model might have changed.
	 *
	 * @return <code>true</code> if world change, <code>false</code> otherwise
	 * @since 3.0
	 */
	public boolean isWorldChange() {
		return fIsWorldChange;
	}

	/**
	 * Marks this event as world change according to the given flag.
	 *
	 * @param isWorldChange <code>true</code> if this event is a world change, <code>false</code> otherwise
	 * @since 3.0
	 */
	void markWorldChange(boolean isWorldChange) {
		fIsWorldChange= isWorldChange;
	}

	/**
	 * Returns whether this annotation model event is still valid.
	 *
	 * @return <code>true</code> if this event is still valid, <code>false</code> otherwise
	 * @since 3.0
	 */
	public boolean isValid() {
		if (fModificationStamp != null && fAnnotationModel instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension extension= (IAnnotationModelExtension) fAnnotationModel;
			return fModificationStamp == extension.getModificationStamp();
		}
		return true;
	}

	/**
	 * Seals this event. Any direct modification to the annotation model after the event has been sealed
	 * invalidates this event.
	 *
	 * @since 3.0
	 */
	public void markSealed() {
		if (fAnnotationModel instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension extension= (IAnnotationModelExtension) fAnnotationModel;
			fModificationStamp= extension.getModificationStamp();
		}
	}
}
