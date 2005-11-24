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
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jface.databinding.internal.beans.JavaBeanUpdatableCollection;
import org.eclipse.jface.databinding.internal.beans.JavaBeanUpdatableTree;
import org.eclipse.jface.databinding.internal.beans.JavaBeanUpdatableValue;
import org.eclipse.jface.util.Assert;

/**
 * A factory for creating updatable objects for properties of plain Java objects
 * with JavaBeans-style notification.
 * 
 * This factory supports the following description objects:
 * <ul>
 * <li>org.eclipse.jface.databinding.PropertyDescription:
 * <ul>
 * <li>Returns an updatable value representing the specified value property of
 * the given object, if {@link Property#isCollectionProperty()}
 * returns false</li>
 * <li>Returns an updatable collection representing the specified collection
 * property of the given object, if
 * {@link Property#isCollectionProperty()} returns false</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @since 3.2
 * 
 */
final public class BeanUpdatableFactory implements IUpdatableFactory {

	public IUpdatable createUpdatable(Map properties, Object description,
			IDataBindingContext bindingContext) {
		if (description instanceof Property) {
			Property propertyDescription = (Property) description;
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
						if (descriptor.getPropertyType().isArray()
								|| Collection.class.isAssignableFrom(descriptor
										.getPropertyType())) {
							Class elementType = descriptor.getPropertyType()
									.isArray() ? descriptor.getPropertyType()
									.getComponentType() : getCollectionType(descriptor,beanInfo.getBeanDescriptor().getBeanClass());
							Assert.isTrue(elementType != null);
							return new JavaBeanUpdatableCollection(object,
									descriptor, elementType);
						}
						return new JavaBeanUpdatableValue(object, descriptor);
					}
				}
			}
		}
		else if (description instanceof ITree)
			return new JavaBeanUpdatableTree((ITree)description);
		return null;
	}
	
	private Class getCollectionType(PropertyDescriptor propertyDescriptor,Class beanClass){
		// If the java.beans.PropertyDescriptor is typed to java.util.Collection then the signature does not tell us the type of its contents (unliked typed arrays)
		// To determine the type of its contents look for an add method, e.g. for "foos" look for "addFoo(...)" where the argument will tell us the type of the contents		
		StringBuffer addMethodName = new StringBuffer("add"); //$NON-NLS-1$
		String propertyName = propertyDescriptor.getName();
		addMethodName.append(propertyName.substring(0,1).toUpperCase(Locale.US));
		if(propertyName.endsWith("s")){ //$NON-NLS-1$
			addMethodName.append(propertyName.subSequence(1,propertyName.length()-1));
		}		
		Method[] methods = beanClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			String methodName = methods[i].getName();
			if(methodName.equals(addMethodName.toString())){
				return methods[i].getParameterTypes()[0];
			}
		}
		return null;
	}

}
