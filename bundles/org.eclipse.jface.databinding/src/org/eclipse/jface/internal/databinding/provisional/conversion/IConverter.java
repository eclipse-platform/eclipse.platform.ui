/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.provisional.conversion;

/**
 * A one-way converter.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 1.0
 * 
 */
public interface IConverter {

	/**
	 * Returns the type whose instances can be converted by this converter.
	 * 
	 * @return the type whose instances can be converted, or null if this converter is untyped
	 */
	public Object getFromType();

	/**
	 * Returns the type to which this converter can convert.
	 * 
	 * @return the type to which this converter can convert, or null if this converter is untyped
	 */
	public Object getToType();

	/**
	 * Returns the result of the conversion of the given object. The given
	 * object must be an instance of getTargetType(), and the result must be an
	 * instance of getModelType().
	 * 
	 * @param fromObject
	 *            the object to convert
	 * @return the converted object
	 */
	public Object convert(Object fromObject);
}
