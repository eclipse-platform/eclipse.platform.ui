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
}
