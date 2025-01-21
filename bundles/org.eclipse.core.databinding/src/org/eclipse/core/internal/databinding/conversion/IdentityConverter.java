/*
 * Copyright (C) 2005, 2025 db4objects Inc.  http://www.db4o.com  and others.
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
 *     Matt Carter - Character support completed (bug 197679)
 */
package org.eclipse.core.internal.databinding.conversion;

import org.eclipse.core.databinding.BindingException;
import org.eclipse.core.databinding.conversion.IConverter;

/**
 * TheIdentityConverter. Returns the source value (the identity function).
 */
public class IdentityConverter implements IConverter<Object, Object> {

	private Class<?> fromType;

	private Class<?> toType;

	public IdentityConverter(Class<?> type) {
		this.fromType = type;
		this.toType = type;
	}

	public IdentityConverter(Class<?> fromType, Class<?> toType) {
		this.fromType = fromType;
		this.toType = toType;
	}

	private Class<?>[][] primitiveMap = {
			{ Integer.TYPE, Integer.class }, { Short.TYPE, Short.class },
			{ Long.TYPE, Long.class }, { Double.TYPE, Double.class },
			{ Byte.TYPE, Byte.class }, { Float.TYPE, Float.class },
			{ Boolean.TYPE, Boolean.class },
			{ Character.TYPE, Character.class } };

	@Override
	public Object convert(Object source) {
		if (toType.isPrimitive()) {
			if (source == null) {
				throw new BindingException("Cannot convert null to a primitive"); //$NON-NLS-1$
			}
		}
		if (source != null) {
			Class<?> sourceClass = source.getClass();
			if (toType.isPrimitive() || sourceClass.isPrimitive()) {
				if (sourceClass.equals(toType)
						|| isPrimitiveTypeMatchedWithBoxed(sourceClass, toType)) {
					return source;
				}
				throw new BindingException(
						"Boxed and unboxed types do not match"); //$NON-NLS-1$
			}
			if (!toType.isAssignableFrom(sourceClass)) {
				throw new BindingException(sourceClass.getName()
						+ " is not assignable to " + toType.getName()); //$NON-NLS-1$
			}
		}
		return source;
	}

	/**
	 * (Non-API) isPrimitiveTypeMatchedWithBoxed.
	 *
	 * @return true if sourceClass and toType are matched primitive/boxed types
	 */
	public boolean isPrimitiveTypeMatchedWithBoxed(Class<?> sourceClass, Class<?> toClass) {
		for (Class<?>[] primitiveTuple : primitiveMap) {
			if (toClass.equals(primitiveTuple[0])
					&& sourceClass.equals(primitiveTuple[1])) {
				return true;
			}
			if (sourceClass.equals(primitiveTuple[0])
					&& toClass.equals(primitiveTuple[1])) {
				return true;
			}
		}
		return false;
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
