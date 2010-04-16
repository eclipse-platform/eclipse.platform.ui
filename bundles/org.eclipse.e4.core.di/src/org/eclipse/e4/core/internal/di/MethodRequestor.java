/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.di;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.eclipse.e4.core.di.AbstractObjectSupplier;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.IObjectDescriptor;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.ObjectDescriptorFactory;

public class MethodRequestor extends Requestor {

	final private Method method;

	public MethodRequestor(Method method, IInjector injector, AbstractObjectSupplier primarySupplier, Object requestingObject, boolean track, boolean groupUpdates, boolean optional) {
		super(injector, primarySupplier, requestingObject, track, groupUpdates, optional);
		this.method = method;
	}

	public Object execute() throws InjectionException {
		return callMethod(method, actualArgs);
	}

	@Override
	public IObjectDescriptor[] getDependentObjects() {
		Type[] parameterTypes = method.getGenericParameterTypes();
		// TBD make getInjectParamProperties produce ObjectDescriptors
		InjectionProperties[] properties = annotationSupport.getInjectParamProperties(method);
		IObjectDescriptor[] descriptors = new IObjectDescriptor[properties.length];
		for (int i = 0; i < properties.length; i++) {
			descriptors[i] = ObjectDescriptorFactory.make(parameterTypes[i], properties[i].getQualifiers());
		}
		return descriptors;
	}

	private Object callMethod(Method method, Object[] args) throws InjectionException {
		Object userObject = getRequestingObject();
		if (userObject == null)
			return null;
		Object result = null;
		boolean wasAccessible = true;
		if (!method.isAccessible()) {
			method.setAccessible(true);
			wasAccessible = false;
		}
		try {
			result = method.invoke(userObject, args);
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

}
