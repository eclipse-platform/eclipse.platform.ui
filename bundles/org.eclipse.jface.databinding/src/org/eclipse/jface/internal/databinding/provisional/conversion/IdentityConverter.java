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
 * TheIdentityConverter. Returns the source value (the identity function).
 */
public class IdentityConverter implements IConverter {

	private Class fromType;

	private Class toType;

	/**
	 * @param type
	 */
	public IdentityConverter(Class type) {
		this.fromType = type;
		this.toType = type;
	}

	/**
	 * @param fromType
	 * @param toType
	 */
	public IdentityConverter(Class fromType, Class toType) {
		this.fromType = fromType;
		this.toType = toType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.binding.converter.IConverter#convert(java.lang.Object)
	 */
	public Object convert(Object source) {
		return source;
	}

	public Object getFromType() {
		return fromType;
	}

	public Object getToType() {
		return toType;
	}

}
