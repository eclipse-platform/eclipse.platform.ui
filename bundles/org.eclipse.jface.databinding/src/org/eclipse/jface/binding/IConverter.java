/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.binding;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 * 
 */
public interface IConverter {

	/**
	 * Returns the class whose instances can be converted by this converter.
	 * 
	 * @return the class whose instances can be converted
	 */
	public Class getModelType();

	/**
	 * Returns the class to which this converter can convert.
	 * 
	 * @return the class to which this converter can convert
	 */
	public Class getTargetType();

	/**
	 * Returns the result of the conversion of the given object. The given
	 * object must be an instance of getTargetType(), and the result must be an
	 * instance of getModelType().
	 * 
	 * @param targetObject
	 *            the object to convert
	 * @return the converted object
	 */
	public Object convertTargetToModel(Object targetObject);

	/**
	 * Returns the result of the conversion of the given object. The given
	 * object must be an instance of getModelType(), and the result must be an
	 * instance of getTargetType().
	 * 
	 * @param modelObject
	 *            the object to convert
	 * @return the converted object
	 */
	public Object convertModelToTarget(Object modelObject);
}
