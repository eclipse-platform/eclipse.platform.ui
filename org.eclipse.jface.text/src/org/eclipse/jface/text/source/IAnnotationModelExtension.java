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

import java.util.Map;


/**
 * Extends <code>IAnnotationModel</code> with the ability to attach additional annotation models to it.
 * 
 * @since 3.0
 */
public interface IAnnotationModelExtension {
	
	/**
	 * Attaches <code>attachment</code> to the receiver. Connects <code>attachment</code> to
	 * the currently connected document. If <code>attachment</code> is already attached (even)
	 * under a different key), it is not attached again.
	 * 
	 * @param key the key through which the attachment is identified.
	 * @param attachment the attached <code>IAnnotationModel</code>
	 */
	void addAnnotationModel(Object key, IAnnotationModel attachment);
	
	/**
	 * Returns the attached <code>IAnnotationModel</code> for <code>key</code>, or <code>null</code>
	 * if none is attached for <code>key</code>.
	 * 
	 * @param key the key through which the attachment is identified.
	 * @return an <code>IAnnotationModel</code> attached under <code>key</code>, or <code>null</code>
	 */
	IAnnotationModel getAnnotationModel(Object key);
	
	/**
	 * Removes and returns the attached <code>IAnnotationModel</code> for <code>key</code>.
	 * 
	 * @param key the key through which the attachment is identified.
	 * @return an <code>IAnnotationModel</code> attached under <code>key</code>, or <code>null</code>
	 */
	IAnnotationModel removeAnnotationModel(Object key);

	/**
	 * Replaces annotations with new annotations for this annotation model. The new
	 * annotations are map entries where the annotation is the key and the value is the
	 * position for the annotation. Each position describes the range covered by the annotation. 
	 * All registered annotation model listeners are informed about the change (if any).
	 * If the model is connected to a document, the positions are automatically
	 * updated on document changes. For each annotation which is already managed by
	 * this annotation model or is not associated with a valid position in the connected
	 * document nothing happens.
	 *
	 * @param annotationsToRemove the annotations to be removed, may be <code>null</code>
	 * @param annotationsToAdd the annotations which will be added, may be <code>null</code>
	 *			each map entry has an <code>Annotation</code> as key and a <code>Position</code> as value
	 * @throws ClassCastException if one of the map key or values has a wrong type
	 */
	void replaceAnnotations(Annotation[] annotationsToRemove, Map annotationsToAdd) throws ClassCastException;
}
