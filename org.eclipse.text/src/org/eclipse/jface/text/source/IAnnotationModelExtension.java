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


import java.util.Map;

import org.eclipse.jface.text.Position;


/**
 * Extends {@link org.eclipse.jface.text.source.IAnnotationModel}with the
 * ability piggyback other annotation models. It also introduces the concept of
 * modification time stamps and adds methods for richer manipulation methods.
 *
 * @since 3.0
 */
public interface IAnnotationModelExtension {

	/**
	 * Attaches <code>attachment</code> to the receiver. Connects
	 * <code>attachment</code> to the currently connected document. If
	 * <code>attachment</code> is already attached (even) under a different
	 * key), it is not attached again.
	 *
	 * @param key the key through which the attachment is identified.
	 * @param attachment the attached <code>IAnnotationModel</code>
	 */
	void addAnnotationModel(Object key, IAnnotationModel attachment);

	/**
	 * Returns the attached <code>IAnnotationModel</code> for <code>key</code>,
	 * or <code>null</code> if none is attached for <code>key</code>.
	 *
	 * @param key the key through which the attachment is identified.
	 * @return an <code>IAnnotationModel</code> attached under
	 *         <code>key</code>, or <code>null</code>
	 */
	IAnnotationModel getAnnotationModel(Object key);

	/**
	 * Removes and returns the attached <code>IAnnotationModel</code> for
	 * <code>key</code>.
	 *
	 * @param key the key through which the attachment is identified.
	 * @return an <code>IAnnotationModel</code> attached under
	 *         <code>key</code>, or <code>null</code>
	 */
	IAnnotationModel removeAnnotationModel(Object key);

	/**
	 * Adds and removes annotations to/from this annotation model in a single
	 * step. The annotations to remove are given in an array. The annotations to
	 * add are provided in a map associating the annotations with the positions
	 * at which they should be added. All registered annotation model listeners
	 * are informed about the change. If the model is connected to a document,
	 * the positions are automatically updated on document changes. Annotations
	 * that are already managed by this annotation model or are not associated
	 * with a valid position in the connected document have no effect.
	 *
	 * @param annotationsToRemove the annotations to be removed, may be
	 *            <code>null</code>
	 * @param annotationsToAdd the annotations which will be added, may be
	 *            <code>null</code> each map entry has an
	 *            <code>Annotation</code> as key and a <code>Position</code>
	 *            as value
	 * @throws ClassCastException if one of the map key or values has a wrong
	 *             type
	 */
	void replaceAnnotations(Annotation[] annotationsToRemove, Map annotationsToAdd) throws ClassCastException;

	/**
	 * Modifies the position associated with the given annotation to equal the
	 * given position. If the annotation is not yet managed by this annotation
	 * model, the annotation is added. If the given position is
	 * <code>null</code> the annotation is removed from the model. All
	 * annotation model change listeners will be informed about the change.
	 *
	 * @param annotation the annotation whose associated position should be
	 *            modified
	 * @param position the position to whose values the associated position
	 *            should be changed
	 */
	void modifyAnnotationPosition(Annotation annotation, Position position);

	/**
	 * Removes all annotations from this annotation model.
	 */
	void removeAllAnnotations();

	/**
	 * Returns the modification stamp of this annotation model.
	 *
	 * @return the modification stamp of this annotation model
	 */
	Object getModificationStamp();
}
