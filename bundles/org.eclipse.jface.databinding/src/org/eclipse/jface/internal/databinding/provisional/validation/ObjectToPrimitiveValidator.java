/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.validation;

import org.eclipse.jface.internal.databinding.internal.BindingMessages;

/**
 * @since 3.2
 *
 */
public class ObjectToPrimitiveValidator implements IValidator {
	
	private Class toType;
	

	private Class[][] primitiveMap = new Class[][] {
			{Integer.TYPE, Integer.class},
			{Short.TYPE, Short.class},
			{Long.TYPE, Long.class},
			{Double.TYPE, Double.class},
			{Byte.TYPE, Byte.class},
			{Float.TYPE, Float.class},
			{Boolean.TYPE, Boolean.class},
	};	
	
	/**
	 * @param toType
	 */
	public ObjectToPrimitiveValidator(Class toType) {
		this.toType = toType;
	}
	
	protected Class getToType() {
		return this.toType;
	}

	public ValidationError isPartiallyValid(Object value) {
		return validate(value);
	}

	public ValidationError isValid(Object value) {
		return validate(value);
	}
	
	private ValidationError validate(Object value) {
		if (value != null) {
			if (!mapContainsValues(toType, value.getClass())) {
				return ValidationError.error(getClassHint());		
			}
			return null;
		}
		return ValidationError.error(getNullHint());		
	}

	/**
	 * @param toType2
	 * @param class1
	 * @return
	 */
	private boolean mapContainsValues(Class toType, Class fromType) {
		for (int i = 0; i < primitiveMap.length; i++) {
			if ((primitiveMap[i][0].equals(toType)) && (primitiveMap[i][1].equals(fromType))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return 
	 */
	public String getNullHint() {
		return BindingMessages.getString("Validate_ConversionToPrimitive"); //$NON-NLS-1$
	}
	
	/**
	 * @return
	 */
	public String getClassHint() {
		return BindingMessages.getString("Validate_ConversionFromClassToPrimitive"); //$NON-NLS-1$
	}
}
