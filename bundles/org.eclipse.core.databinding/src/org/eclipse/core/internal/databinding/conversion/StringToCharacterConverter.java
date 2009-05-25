/*
 * Copyright (C) 2005, 2007 db4objects Inc.  http://www.db4o.com
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public class StringToCharacterConverter implements IConverter {

	private final boolean primitiveTarget;

	/**
	 * 
	 * @param primitiveTarget
	 */
	public StringToCharacterConverter(boolean primitiveTarget) {
		this.primitiveTarget = primitiveTarget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.binding.converter.IConverter#convert(java.lang.Object)
	 */
	public Object convert(Object source) {
		if (source != null && !(source instanceof String))
			throw new IllegalArgumentException(
					"String2Character: Expected type String, got type [" + source.getClass().getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$

		String s = (String) source;
		if (source == null || s.equals("")) { //$NON-NLS-1$
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
			result = new Character(s.charAt(0));
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"String2Character: " + e.getMessage() + ": " + s); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return result;
	}

	public Object getFromType() {
		return String.class;
	}

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
