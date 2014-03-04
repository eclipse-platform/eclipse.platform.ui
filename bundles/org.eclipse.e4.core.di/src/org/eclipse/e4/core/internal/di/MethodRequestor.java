/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.WeakHashMap;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.PrimaryObjectSupplier;

public class MethodRequestor extends Requestor<Method> {

	/**
	 * The parameters annotation cache.  Having a *static* map is valuable as it changes the hit rate from about 60% to about 90%.
	 */
	private static Map<Method, Annotation[][]> annotationCache = new WeakHashMap<Method, Annotation[][]>();

	public MethodRequestor(Method method, IInjector injector, PrimaryObjectSupplier primarySupplier, PrimaryObjectSupplier tempSupplier, Object requestingObject, boolean track) {
		super(method, injector, primarySupplier, tempSupplier, requestingObject, track);
	}

	public Object execute() throws InjectionException {
		if (actualArgs == null) {
			if (location.getParameterTypes().length > 0)
				return null; // optional method call
		}
		Object userObject = getRequestingObject();
		if (userObject == null)
			return null;
		Object result = null;
		boolean wasAccessible = true;
		if (!location.isAccessible()) {
			location.setAccessible(true);
			wasAccessible = false;
		}
		boolean pausedRecording = false;
		if ((primarySupplier != null)) {
			primarySupplier.pauseRecording();
			pausedRecording = true;
		}
		try {
			result = location.invoke(userObject, actualArgs);
		} catch (IllegalArgumentException e) {
			throw new InjectionException(e);
		} catch (IllegalAccessException e) {
			throw new InjectionException(e);
		} catch (InvocationTargetException e) {
			Throwable originalException = e.getCause();
			throw new InjectionException((originalException != null) ? originalException : e);
		} finally {
			if (!wasAccessible)
				location.setAccessible(false);
			if (pausedRecording)
				primarySupplier.resumeRecording();
			clearResolvedArgs();
		}
		return result;
	}

	@Override
	protected IObjectDescriptor[] calcDependentObjects() {
		Type[] parameterTypes = location.getGenericParameterTypes();
		Annotation[][] annotations = getParameterAnnotations();
		IObjectDescriptor[] descriptors = new IObjectDescriptor[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			descriptors[i] = new ObjectDescriptor(parameterTypes[i], annotations[i]);
		}
		return descriptors;
	}

	/** @return the annotations for each of the method parameters */
	public Annotation[][] getParameterAnnotations() {
		// We don't synchronize annotationCache to avoid performance overhead.
		// The code below should be fine non-synchronized; but this needs to be
		// kept in mind if this method is updated.
		Annotation[][] result = annotationCache.get(location);
		if (result == null) {
			result = location.getParameterAnnotations();
			annotationCache.put(location, result);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuffer tmp = new StringBuffer();
		Object object = getRequestingObject();
		if (object != null)
			tmp.append(object.getClass().getSimpleName());
		tmp.append('#');
		tmp.append(location.getName());
		tmp.append('(');
		tmp.append(')');
		return tmp.toString();
	}
}
