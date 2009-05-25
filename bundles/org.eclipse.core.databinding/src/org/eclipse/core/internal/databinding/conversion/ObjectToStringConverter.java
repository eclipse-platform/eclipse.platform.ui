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
 */
package org.eclipse.core.internal.databinding.conversion;

import org.eclipse.core.databinding.conversion.IConverter;

/**
 * Converts any object to a string by calling its toString() method.
 */
public class ObjectToStringConverter implements IConverter {
	private final Class fromClass;

	/**
	 * 
	 */
	public ObjectToStringConverter() {
		this(Object.class);
	}

	/**
	 * @param fromClass
	 */
	public ObjectToStringConverter(Class fromClass) {
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
