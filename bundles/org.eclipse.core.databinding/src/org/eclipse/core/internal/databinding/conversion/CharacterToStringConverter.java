/*******************************************************************************
 * Copyright (c) 2007 Matt Carter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matt Carter - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding.conversion;

import org.eclipse.core.databinding.conversion.Converter;

/**
 * Converts a character to a string.
 */
public class CharacterToStringConverter extends Converter {
	private final boolean primitive;

	/**
	 * @param primitive
	 */
	private CharacterToStringConverter(boolean primitive) {
		super(primitive ? Character.TYPE : Character.class, String.class);
		this.primitive = primitive;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
	 */
	public Object convert(Object fromObject) {
		// Null is allowed when the type is not primitive.
		if (fromObject == null) {
			if (primitive)
				throw new IllegalArgumentException(
						"'fromObject' is null. Cannot convert to primitive char."); //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}

		if (!(fromObject instanceof Character)) {
			throw new IllegalArgumentException(
					"'fromObject' is not of type [Character]."); //$NON-NLS-1$
		}

		return String.valueOf(((Character) fromObject).charValue());
	}

	/**
	 * @param primitive
	 * @return converter
	 */
	public static CharacterToStringConverter fromCharacter(boolean primitive) {
		return new CharacterToStringConverter(primitive);
	}

}