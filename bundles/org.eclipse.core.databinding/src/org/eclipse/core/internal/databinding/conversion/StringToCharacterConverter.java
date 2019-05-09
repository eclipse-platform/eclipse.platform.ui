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
 *     Matt Carter - Improved primitive conversion support (bug 197679)
 */
package org.eclipse.core.internal.databinding.conversion;

import org.eclipse.core.databinding.conversion.IConverter;

/**
 * StringToCharacterConverter.
 */
public class StringToCharacterConverter implements IConverter<Object, Character> {

	private final boolean primitiveTarget;

	/**
	 *
	 * @param primitiveTarget
	 */
	public StringToCharacterConverter(boolean primitiveTarget) {
		this.primitiveTarget = primitiveTarget;
	}

	@Override
	public Character convert(Object source) {
		if (source != null && !(source instanceof String))
			throw new IllegalArgumentException(
					"String2Character: Expected type String, got type [" + source.getClass().getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$

		String s = (String) source;
		if (source == null || s.isEmpty()) {
			if (primitiveTarget)
				throw new IllegalArgumentException(
						"String2Character: cannot convert null/empty string to character primitive"); //$NON-NLS-1$
			return null;
		}
		Character result;

		if (s.length() > 1)
			throw new IllegalArgumentException(
					"String2Character: string too long: " + s); //$NON-NLS-1$

		try {
			result = Character.valueOf(s.charAt(0));
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"String2Character: " + e.getMessage() + ": " + s); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return result;
	}

	@Override
	public Object getFromType() {
		return String.class;
	}

	@Override
	public Object getToType() {
		return primitiveTarget ? Character.TYPE : Character.class;
	}

	/**
	 * @param primitive
	 * @return converter
	 */
	public static StringToCharacterConverter toCharacter(boolean primitive) {
		return new StringToCharacterConverter(primitive);
	}

}
