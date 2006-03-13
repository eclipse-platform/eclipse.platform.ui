/*
 * Copyright (C) 2005 db4objects Inc.  http://www.db4o.com
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 */
package org.eclipse.jface.internal.databinding.provisional.conversion;

/**
 * Converts any object to a string by calling its toString() method.
 */
public class ToStringConverter implements IConverter {

	/**
	 * A singleton for the toString() converter function
	 */
	public static final ToStringConverter TOSTRINGFUNCTION = new ToStringConverter();

	private final Class fromClass;

	/**
	 * 
	 */
	public ToStringConverter() {
		this(Object.class);
	}

	/**
	 * @param fromClass
	 */
	public ToStringConverter(Class fromClass) {
		this.fromClass = fromClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.binding.converter.IConverter#convert(java.lang.Object)
	 */
	public Object convert(Object source) {
		if (source == null) {
			return ""; //$NON-NLS-1$
		}
		return source.toString();
	}

	public Object getFromType() {
		return fromClass;
	}

	public Object getToType() {
		return String.class;
	}

}
