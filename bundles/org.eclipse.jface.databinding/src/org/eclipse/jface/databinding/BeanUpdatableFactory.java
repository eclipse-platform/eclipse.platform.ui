/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Map;

import org.eclipse.jface.databinding.internal.beans.JavaBeanUpdatableCollection;
import org.eclipse.jface.databinding.internal.beans.JavaBeanUpdatableValue;
import org.eclipse.jface.util.Assert;

/**
 * @since 3.2
 * 
 */
final public class BeanUpdatableFactory implements IUpdatableFactory {

	public IUpdatable createUpdatable(Map properties, Object description,
			IDataBindingContext bindingContext, IValidationContext validationContext) {
		if (description instanceof PropertyDescription) {
			PropertyDescription propertyDescription = (PropertyDescription) description;
			if (propertyDescription.getObject() != null) {
				Object object = propertyDescription.getObject();
				BeanInfo beanInfo;
				try {
					beanInfo = Introspector.getBeanInfo(object.getClass());
				} catch (IntrospectionException e) {
					// cannot introspect, give up
					return null;
				}
				PropertyDescriptor[] propertyDescriptors = beanInfo
						.getPropertyDescriptors();
				for (int i = 0; i < propertyDescriptors.length; i++) {
					PropertyDescriptor descriptor = propertyDescriptors[i];
					if (descriptor.getName().equals(
							propertyDescription.getPropertyID())) {
						if (descriptor.getPropertyType().isArray() || Collection.class.isAssignableFrom(descriptor.getPropertyType())) {
							Class elementType = descriptor.getPropertyType().isArray()? 
									descriptor.getPropertyType().getComponentType() : propertyDescription.getPropertyType();
							Assert.isTrue(elementType!=null);									
							return new JavaBeanUpdatableCollection(object,
									descriptor, elementType);
						}
						return new JavaBeanUpdatableValue(object, descriptor);
					}
				}
			}
		}
		return null;
	}

}
