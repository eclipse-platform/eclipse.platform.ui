/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920, 159768
 *******************************************************************************/
// TODO djo: copyright
package org.eclipse.jface.tests.databinding.scenarios;

import java.lang.reflect.Method;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;

/**
 * A BindSpec that will automatically grab validators from an object's
 * properties, if a get&lt;PropertyName>Validator method is defined. Makes it
 * easy to associate validators with the properties that they are responsible
 * for validating.
 * 
 */
public class CustomBeanUpdateValueStrategy extends UpdateValueStrategy {

	public IConverter createConverter(Object fromType, Object toType) {
		if (fromType instanceof CustomBeanModelType) {
			CustomBeanModelType customBeanModelType = (CustomBeanModelType) fromType;
			fromType = customBeanModelType.getType();
		}
		if (toType instanceof CustomBeanModelType) {
			CustomBeanModelType customBeanModelType = (CustomBeanModelType) toType;
			toType = customBeanModelType.getType();
		}
		return super.createConverter(fromType, toType);
	}

	protected void fillDefaults(IObservableValue source,
			IObservableValue destination) {
		if (destination.getValueType() instanceof CustomBeanModelType) {
			if (beforeSetValidator==null) {
				CustomBeanModelType property = (CustomBeanModelType) destination.getValueType();
				String propertyName = property.getPropertyName();
				String getValidatorMethodName = "get" + upperCaseFirstLetter(propertyName) + "Validator"; //$NON-NLS-1$ //$NON-NLS-2$

				Class objectClass = property.getObject().getClass();

				Method getValidator;
				try {
					getValidator = objectClass.getMethod(getValidatorMethodName,
							new Class[] { Class.class });
					beforeSetValidator= (IValidator) getValidator.invoke(property
								.getObject(), new Object[0]);
				} catch (Exception e) {
					// ignore
				}

			}
 		}
		super.fillDefaults(source, destination);
	}

	private String upperCaseFirstLetter(String name) {
		String result = name.substring(0, 1).toUpperCase() + name.substring(1);
		return result;
	}

	public Boolean isAssignableFromTo(Object fromType, Object toType) {
		if (fromType instanceof CustomBeanModelType) {
			fromType = ((CustomBeanModelType) fromType).getType();
		}
		if (toType instanceof CustomBeanModelType) {
			toType = ((CustomBeanModelType) toType).getType();
		}
		return super.isAssignableFromTo(fromType, toType);
	}
}
