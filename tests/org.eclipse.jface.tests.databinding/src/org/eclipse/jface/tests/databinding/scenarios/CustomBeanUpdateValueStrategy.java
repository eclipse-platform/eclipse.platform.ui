/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
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
 * properties, if a get&lt;PropertyName&gt;Validator method is defined. Makes it
 * easy to associate validators with the properties that they are responsible
 * for validating.
 */
public class CustomBeanUpdateValueStrategy<S, D> extends UpdateValueStrategy<S, D> {

	@Override
	public IConverter<?, ?> createConverter(Object fromType, Object toType) {
		if (fromType instanceof CustomBeanModelType) {
			@SuppressWarnings("unchecked")
			CustomBeanModelType<S> customBeanModelType = (CustomBeanModelType<S>) fromType;
			fromType = customBeanModelType.getType();
		}
		if (toType instanceof CustomBeanModelType) {
			@SuppressWarnings("unchecked")
			CustomBeanModelType<D> customBeanModelType = (CustomBeanModelType<D>) toType;
			toType = customBeanModelType.getType();
		}
		return super.createConverter(fromType, toType);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void fillDefaults(IObservableValue<? extends S> source, IObservableValue<? super D> destination) {
		if (destination.getValueType() instanceof CustomBeanModelType) {
			if (beforeSetValidator==null) {
				CustomBeanModelType<?> property = (CustomBeanModelType<?>) destination.getValueType();
				String propertyName = property.getPropertyName();
				String getValidatorMethodName = "get" + upperCaseFirstLetter(propertyName) + "Validator"; //$NON-NLS-1$ //$NON-NLS-2$

				Class<?> objectClass = property.getObject().getClass();

				Method getValidator;
				try {
					getValidator = objectClass.getMethod(getValidatorMethodName, Class.class);
					beforeSetValidator = (IValidator<D>) getValidator.invoke(property.getObject());
				} catch (Exception e) {
					// ignore
				}

			}
		}
		super.fillDefaults(source, destination);
	}

	private String upperCaseFirstLetter(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	@Override
	public Boolean isAssignableFromTo(Object fromType, Object toType) {
		if (fromType instanceof CustomBeanModelType) {
			fromType = ((CustomBeanModelType<?>) fromType).getType();
		}
		if (toType instanceof CustomBeanModelType) {
			toType = ((CustomBeanModelType<?>) toType).getType();
		}
		return super.isAssignableFromTo(fromType, toType);
	}
}
