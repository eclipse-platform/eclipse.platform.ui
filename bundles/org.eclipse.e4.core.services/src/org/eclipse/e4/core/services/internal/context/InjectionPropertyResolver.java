/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.eclipse.e4.core.services.internal.annotations.AnnotationsSupport;

public class InjectionPropertyResolver {

	static public InjectionProperties getInjectionProperties(Field field, String fieldPrefix) {
		InjectionProperties properties = AnnotationsSupport.getInjectProperties(field);
		if (properties != null) {
			if (properties.shoudlInject()) {
				if (properties.getPropertyName() == null)
					properties.setPropertyName(field.getName()); // no field prefix for annotations
				return properties;
			}
		}

		String fieldName = field.getName();
		if (fieldName.startsWith(fieldPrefix))
			return new InjectionProperties(true, fieldName.substring(fieldPrefix.length()), true);
		return null;
	}

	static public InjectionProperties getInjectionProperties(Method method, String methodPrefix) {
		InjectionProperties properties = AnnotationsSupport.getInjectProperties(method);
		if (properties != null) {
			if (properties.shoudlInject()) {
				if (properties.getPropertyName() == null) {
					String methodName = method.getName();
					if (methodName.startsWith(methodPrefix))
						properties.setPropertyName(methodName.substring(methodPrefix.length()));
				}
				return properties;
			}
		}

		String methodName = method.getName();
		if (methodName.startsWith(methodPrefix))
			return new InjectionProperties(true, methodName.substring(methodPrefix.length()), true);
		return null;
	}

	static public InjectionProperties getInjectionProperties(Class type) {
		return AnnotationsSupport.getInjectProperties(type);
	}

	static public boolean isPostConstruct(Method method) {
		return AnnotationsSupport.isPostConstruct(method);
	}

	static public boolean isPreDestory(Method method) {
		return AnnotationsSupport.isPreDestory(method);
	}
}
