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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.internal.annotations.AnnotationsSupport;

public class InjectionPropertyResolver {

	static public InjectionProperties getInjectionProperties(Field field, String fieldPrefix) {
		InjectionProperties properties = AnnotationsSupport.getInjectProperties(field);

		// see if we should augment annotations injection properties
		if (field.getName().startsWith(fieldPrefix)) {
			properties.setInject(true);
			properties.setPropertyName(field.getName().substring(fieldPrefix.length()));
		}
		if (properties.getPropertyName() == null)
			properties.setPropertyName(field.getType().getName());

		return properties;
	}

	static public InjectionProperties[] getInjectionParamProperties(Method method) {
		InjectionProperties[] properties = AnnotationsSupport.getInjectParamProperties(method);
		Class[] params = method.getParameterTypes();
		for (int i = 0; i < properties.length; i++) {
			if (properties[i].getPropertyName() == null)
				properties[i].setPropertyName(params[i].getName());
		}
		return properties;
	}

	static public InjectionProperties getInjectionProperties(Constructor constructor) {
		return AnnotationsSupport.getInjectProperties(constructor);
	}

	static public InjectionProperties[] getInjectionParamsProperties(Constructor constructor) {
		InjectionProperties[] properties = AnnotationsSupport
				.getInjectParamsProperties(constructor);
		Class[] params = constructor.getParameterTypes();
		for (int i = 0; i < properties.length; i++) {
			if (properties[i].getPropertyName() == null)
				properties[i].setPropertyName(params[i].getName());
		}
		return properties;
	}

	static public InjectionProperties getInjectionProperties(Method method, String methodPrefix) {
		InjectionProperties properties = AnnotationsSupport.getInjectProperties(method);
		// see if we should augment annotations injection properties
		if (method.getName().startsWith(methodPrefix))
			properties.setInject(true);
		return properties;
	}

	// TBD simplify this: only one non-annotation method signature
	/**
	 * Returns whether the given method is a post-construction process method, as defined by the
	 * class comment of {@link ContextInjectionFactory}.
	 */
	static public boolean isPostConstruct(Method method) {
		boolean isPostConstruct = AnnotationsSupport.isPostConstruct(method);
		if (isPostConstruct)
			return true;
		if (!method.getName().equals(IContextConstants.INJECTION_SET_CONTEXT_METHOD))
			return false;
		Class[] parms = method.getParameterTypes();
		if (parms.length == 0)
			return true;
		if (parms.length == 1 && parms[0].equals(IEclipseContext.class))
			return true;
		return false;
	}

	static public boolean isPreDestory(Method method) {
		return AnnotationsSupport.isPreDestory(method);
	}
}
