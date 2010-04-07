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
package org.eclipse.e4.core.internal.di;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;

import org.eclipse.e4.core.di.annotations.GroupUpdates;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.annotations.PostConstruct;
import org.eclipse.e4.core.di.annotations.PreDestroy;

public class AnnotationsSupport {

	public AnnotationsSupport() {
		// placeholder
	}
	
	public InjectionProperties getInjectProperties(Field field) {
		InjectionProperties property = getInjectProperties(field.getAnnotations(), field.getGenericType());
		return property;
	}

	public InjectionProperties getInjectProperties(Method method) {
		return getInjectProperties(method.getAnnotations(), null);
	}
	
	public InjectionProperties getInjectProperties(Constructor constructor) {
		return getInjectProperties(constructor.getAnnotations(), null);
	}
	
	public InjectionProperties[] getInjectParamsProperties(Constructor constructor) {
		Annotation[][] annotations = constructor.getParameterAnnotations();
		Type[] logicalParams = constructor.getGenericParameterTypes();
		// JDK bug: different methods see / don't see generated args for nested classes
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5087240
		Class<?>[] compilerParams = constructor.getParameterTypes();
		if (compilerParams.length > logicalParams.length) { 
			Type[] tmp = new Type[compilerParams.length];
			System.arraycopy(compilerParams, 0, tmp, 0, compilerParams.length - logicalParams.length);
			System.arraycopy(logicalParams, 0, tmp, compilerParams.length - logicalParams.length, logicalParams.length);
			logicalParams = tmp;
		}
		return  getInjectProperties(annotations, logicalParams);
	}

	public InjectionProperties[] getInjectParamProperties(Method method) {
		Annotation[][] annotations = method.getParameterAnnotations();
		Type[] params = method.getGenericParameterTypes();
		return  getInjectProperties(annotations, params);
	}

	private InjectionProperties[] getInjectProperties(Annotation[][] annotations, Type[] params) {
		InjectionProperties[] result = new InjectionProperties[params.length]; 
		for(int i = 0 ; i <  params.length; i++)
			result[i] = getInjectProperties(annotations[i], params[i]);
		return result;
	}

	private InjectionProperties getInjectProperties(Annotation[] annotations, Type param) {
		// Process annotations
		boolean inject = false;
		boolean optional = false;
		String named = null;
		String qualifier = null;
		String handlesEvent = null;
		boolean eventHeadless = true;
		List<Annotation> qualifiers = new ArrayList<Annotation>();
		Annotation qualifierClass = null;
		boolean groupUpdates = false;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof Inject)
					inject = true;
//				else if (annotation instanceof Optional)
//					optional = true;
//				else if (annotation instanceof Named)
//					named = ((Named) annotation).value();
				else if (annotation instanceof GroupUpdates)
					groupUpdates = true;
				else if (annotation.annotationType().isAnnotationPresent(
						Qualifier.class)) {
					qualifiers.add(annotation);
					qualifierClass = annotation;
					Type type = annotation.annotationType();
					if (type instanceof Class<?>) {
						qualifier = ((Class<?>)type).getName();
					}
				}
			}
		}
		String injectName = (named != null) ? named : qualifier;
		InjectionProperties result = new InjectionProperties(inject, injectName);
		if (!qualifiers.isEmpty()) {
			Annotation[] qualifiersArray = new Annotation[qualifiers.size()];
			qualifiers.toArray(qualifiersArray);
			result.setQualifiers(qualifiersArray);
		}
		if (handlesEvent != null) {
			result.setHandlesEvent(handlesEvent);
			result.setEventHeadless(eventHeadless);
		}
		if (groupUpdates)
			result.setGroupUpdates(true);

		return result;
	}
	
	public boolean isPostConstruct(Method method) {
		return method.isAnnotationPresent(PostConstruct.class);
	}
	
	public boolean isPreDestory(Method method) {
		return method.isAnnotationPresent(PreDestroy.class);
	}
}
