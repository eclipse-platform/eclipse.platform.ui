/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.databinding.conversion;

import java.util.function.Function;

/**
 * A one-way converter.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients should subclass {@link Converter}.
 *
 * @since 1.0
 *
 */
public interface IConverter {

	/**
	 * Returns the type whose instances can be converted by this converter. The
	 * return type is Object rather than Class to optionally support richer type
	 * systems than the one provided by Java reflection.
	 *
	 * @return the type whose instances can be converted, or null if this
	 *         converter is untyped
	 */
	public Object getFromType();

	/**
	 * Returns the type to which this converter can convert. The return type is
	 * Object rather than Class to optionally support richer type systems than
	 * the one provided by Java reflection.
	 *
	 * @return the type to which this converter can convert, or null if this
	 *         converter is untyped
	 */
	public Object getToType();

	/**
	 * Returns the result of the conversion of the given object.
	 *
	 * @param fromObject
	 *            the object to convert, of type {@link #getFromType()}
	 * @return the converted object, of type {@link #getToType()}
	 */
	public Object convert(Object fromObject);

	/**
	 * Create a converter
	 *
	 * @param fromType
	 *            the from type
	 * @param toType
	 *            the to type
	 * @param conversion
	 *            the conversion method
	 * @return a new converter instance
	 * @since 1.6
	 */
	public static IConverter create(Object fromType, Object toType, @SuppressWarnings("rawtypes") Function conversion) {
		return new IConverter() {

			@Override
			public Object getFromType() {
				return fromType;
			}

			@Override
			public Object getToType() {
				return toType;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Object convert(Object fromObject) {
				return conversion.apply(fromObject);
			}
		};
	}
}
