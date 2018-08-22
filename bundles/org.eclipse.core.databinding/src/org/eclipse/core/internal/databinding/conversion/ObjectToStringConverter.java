/*
 * Copyright (C) 2005, 2015 db4objects Inc.  http://www.db4o.com
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 */
package org.eclipse.core.internal.databinding.conversion;

import org.eclipse.core.databinding.conversion.IConverter;

/**
 * Converts any object to a string by calling its toString() method.
 */
public class ObjectToStringConverter implements IConverter<Object, String> {
	private final Class<?> fromClass;

	/**
	 *
	 */
	public ObjectToStringConverter() {
		this(Object.class);
	}

	/**
	 * @param fromClass
	 */
	public ObjectToStringConverter(Class<?> fromClass) {
		this.fromClass = fromClass;
	}

	@Override
	public String convert(Object source) {
		if (source == null) {
			return ""; //$NON-NLS-1$
		}
		return source.toString();
	}

	@Override
	public Object getFromType() {
		return fromClass;
	}

	@Override
	public Object getToType() {
		return String.class;
	}

}
