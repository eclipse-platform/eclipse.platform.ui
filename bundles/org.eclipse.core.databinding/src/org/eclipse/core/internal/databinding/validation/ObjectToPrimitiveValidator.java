/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
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
 *     Tom Schindl<tom.schindl@bestsolution.at> - bugfix for 217940
 *******************************************************************************/

package org.eclipse.core.internal.databinding.validation;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 3.2
 */
public class ObjectToPrimitiveValidator implements IValidator<Object> {

	private Class<?> toType;

	private Class<?>[][] primitiveMap = new Class[][] {
			{ Integer.TYPE, Integer.class }, { Short.TYPE, Short.class },
			{ Long.TYPE, Long.class }, { Double.TYPE, Double.class },
			{ Byte.TYPE, Byte.class }, { Float.TYPE, Float.class },
			{ Boolean.TYPE, Boolean.class },
			{ Character.TYPE, Character.class } };

	public ObjectToPrimitiveValidator(Class<?> toType) {
		this.toType = toType;
	}

	protected Class<?> getToType() {
		return this.toType;
	}

	@Override
	public IStatus validate(Object value) {
		return doValidate(value);
	}

	private IStatus doValidate(Object value) {
		if (value != null) {
			if (!mapContainsValues(toType, value.getClass())) {
				return ValidationStatus.error(getClassHint());
			}
			return Status.OK_STATUS;
		}
		return ValidationStatus.error(getNullHint());
	}

	private boolean mapContainsValues(Class<?> toType, Class<?> fromType) {
		for (Class<?>[] primitiveTuple : primitiveMap) {
			if ((primitiveTuple[0].equals(toType))
					&& (primitiveTuple[1].equals(fromType))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return a hint string
	 */
	public String getNullHint() {
		return BindingMessages.getString(BindingMessages.VALIDATE_CONVERSION_TO_PRIMITIVE);
	}

	/**
	 * @return a hint string
	 */
	public String getClassHint() {
		return BindingMessages
				.getString(BindingMessages.VALIDATE_CONVERSION_FROM_CLASS_TO_PRIMITIVE);
	}
}
