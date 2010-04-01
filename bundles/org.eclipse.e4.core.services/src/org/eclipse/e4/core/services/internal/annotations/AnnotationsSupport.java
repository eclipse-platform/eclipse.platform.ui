/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.eclipse.e4.core.services.internal.context.InjectionProperties;

/**
 * Placeholder for annotations support to be replaced by a fragment.
 */
public class AnnotationsSupport {

	public AnnotationsSupport() {
		// placeholder
	}

	public InjectionProperties getInjectProperties(Field field) {
		return new InjectionProperties(false, null, false);
	}

	public InjectionProperties getInjectProperties(Method method) {
		return new InjectionProperties(false, null, false);
	}

	public InjectionProperties getInjectProperties(Constructor constructor) {
		return new InjectionProperties(true, null, false);
	}

	public InjectionProperties[] getInjectParamsProperties(Constructor constructor) {
		Type[] params = constructor.getGenericParameterTypes();

		InjectionProperties[] result = new InjectionProperties[params.length];
		for (int i = 0; i < result.length; i++)
			result[i] = new InjectionProperties(false, null, false);
		return result;
	}

	public InjectionProperties[] getInjectParamProperties(Method method) {
		Class[] params = method.getParameterTypes();
		InjectionProperties[] result = new InjectionProperties[params.length];
		for (int i = 0; i < result.length; i++)
			result[i] = new InjectionProperties(false, null, false);
		return result;
	}

	public boolean isPostConstruct(Method method) {
		return false;
	}

	public boolean isPreDestory(Method method) {
		return false;
	}

}
