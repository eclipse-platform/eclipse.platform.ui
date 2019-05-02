/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.databinding.conversion;

/**
 * Abstract base class for converters.
 *
 * @param <F>
 *            type of the source value
 * @param <T>
 *            type of the converted value
 *
 * @since 1.0
 * @implNote If methods are added to the interface which this class implements
 *           then implementations of those methods must be added to this class.
 */
public abstract class Converter<F, T> implements IConverter<F, T> {

	private Object fromType;
	private Object toType;

	/**
	 * @param fromType type of source values
	 * @param toType   type of converted values
	 */
	public Converter(Object fromType, Object toType) {
		this.fromType = fromType;
		this.toType = toType;
	}

	@Override
	public Object getFromType() {
		return fromType;
	}

	@Override
	public Object getToType() {
		return toType;
	}

}
