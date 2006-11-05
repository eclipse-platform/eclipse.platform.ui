/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 *******************************************************************************/
// TODO djo: copyright
package org.eclipse.jface.tests.databinding.scenarios;

import java.lang.reflect.Method;

import org.eclipse.jface.databinding.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.conversion.IConverter;
import org.eclipse.jface.internal.databinding.provisional.factories.BindSupportFactory;
import org.eclipse.jface.internal.databinding.provisional.validation.IDomainValidator;

/**
 * A BindSupportFactory that will automatically grab validators from an object's
 * properties, if a get&lt;PropertyName>Validator method is defined. Makes it
 * easy to associate validators with the properties that they are responsible
 * for validating.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * 
 * @since 3.2
 */
public class CustomBeanBindSupportFactory extends BindSupportFactory {

	private final DataBindingContext parentContext;

	public CustomBeanBindSupportFactory(DataBindingContext parentContext) {
		this.parentContext = parentContext;
	}

	public IConverter createConverter(Object fromType, Object toType) {
		if (fromType instanceof CustomBeanModelType) {
			CustomBeanModelType customBeanModelType = (CustomBeanModelType) fromType;
			fromType = customBeanModelType.getType();
		}
		if (toType instanceof CustomBeanModelType) {
			CustomBeanModelType customBeanModelType = (CustomBeanModelType) toType;
			toType = customBeanModelType.getType();
		}
		return parentContext.createConverter(fromType, toType);
	}

	public IDomainValidator createDomainValidator(Object modelType) {
		if (modelType instanceof CustomBeanModelType) {
			CustomBeanModelType property = (CustomBeanModelType) modelType;
			String propertyName = property.getPropertyName();
			String getValidatorMethodName = "get" + upperCaseFirstLetter(propertyName) + "Validator"; //$NON-NLS-1$ //$NON-NLS-2$

			Class objectClass = property.getObject().getClass();

			Method getValidator;
			try {
				getValidator = objectClass.getMethod(getValidatorMethodName,
						new Class[] { Class.class });
			} catch (Exception e) {
				return null;
			}

			try {
				IDomainValidator result = (IDomainValidator) getValidator
						.invoke(property.getObject(), new Object[0]);
				return result;
			} catch (Exception e) {
				return null;
			}
		}
		return null;
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
		return new Boolean(parentContext.isAssignableFromTo(fromType, toType));
	}
}
