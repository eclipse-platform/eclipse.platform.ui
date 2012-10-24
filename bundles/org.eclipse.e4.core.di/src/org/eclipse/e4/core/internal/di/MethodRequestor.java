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

public class MethodRequestor extends Requestor {

	final private Method method;

	// Having a *static* map is valuable as it changes the hit rate from about 60% to about 90%.
	private static Map<Method, Annotation[][]> annotationCache = new WeakHashMap<Method, Annotation[][]>();

	public MethodRequestor(Method method, IInjector injector, PrimaryObjectSupplier primarySupplier, PrimaryObjectSupplier tempSupplier, Object requestingObject, boolean track) {
		super(method, injector, primarySupplier, tempSupplier, requestingObject, track);
		this.method = method;
	}

	public Object execute() throws InjectionException {
		if (actualArgs == null) {
			if (method.getParameterTypes().length > 0)
				return null; // optional method call
		}
		Object userObject = getRequestingObject();
		if (userObject == null)
			return null;
		Object result = null;
		boolean wasAccessible = true;
		if (!method.isAccessible()) {
			method.setAccessible(true);
			wasAccessible = false;
		}
		boolean pausedRecording = false;
		if ((primarySupplier != null)) {
			primarySupplier.pauseRecording();
			pausedRecording = true;
		}
		try {
			result = method.invoke(userObject, actualArgs);
		} catch (IllegalArgumentException e) {
			throw new InjectionException(e);
		} catch (IllegalAccessException e) {
			throw new InjectionException(e);
		} catch (InvocationTargetException e) {
			Throwable originalException = e.getCause();
			throw new InjectionException((originalException != null) ? originalException : e);
		} finally {
			if (!wasAccessible)
				method.setAccessible(false);
			if (pausedRecording)
				primarySupplier.resumeRecording();
			clearResolvedArgs();
		}
		return result;
	}

	@Override
	protected IObjectDescriptor[] calcDependentObjects() {
		Type[] parameterTypes = method.getGenericParameterTypes();
		Annotation[][] annotations = getParameterAnnotations();
		IObjectDescriptor[] descriptors = new IObjectDescriptor[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			descriptors[i] = new ObjectDescriptor(parameterTypes[i], annotations[i]);
		}
		return descriptors;
	}

	private Annotation[][] getParameterAnnotations() {
		// We don't synchronize annotationCache to avoid performance overhead.
		// The code below should be fine non-synchronized; but this needs to be
		// kept in mind if this method is updated.
		Annotation[][] result = annotationCache.get(method);
		if (result == null) {
			result = method.getParameterAnnotations();
			annotationCache.put(method, result);
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
		tmp.append(method.getName());
		tmp.append('(');
		tmp.append(')');
		return tmp.toString();
	}

	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodRequestor other = (MethodRequestor) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

}
