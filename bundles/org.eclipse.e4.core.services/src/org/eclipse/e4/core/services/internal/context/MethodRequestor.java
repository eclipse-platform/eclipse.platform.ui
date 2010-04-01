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
package org.eclipse.e4.core.services.internal.context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.eclipse.e4.core.services.injector.IObjectDescriptor;
import org.eclipse.e4.core.services.injector.ObjectDescriptorFactory;

public class MethodRequestor extends Requestor {

	final private Method method;

	public MethodRequestor(Method method, Object requestingObject, boolean track,
			boolean groupUpdates, boolean optional) {
		super(requestingObject, track, groupUpdates, optional);
		this.method = method;
	}

	public Object execute() throws InvocationTargetException, InstantiationException {
		return callMethod(method, actualArgs);
	}

	@Override
	public IObjectDescriptor[] getDependentObjects() {
		Type[] parameterTypes = method.getGenericParameterTypes();
		// TBD make getInjectParamProperties produce ObjectDescriptors
		InjectionProperties[] properties = annotationSupport.getInjectParamProperties(method);
		IObjectDescriptor[] descriptors = new IObjectDescriptor[properties.length];
		for (int i = 0; i < properties.length; i++) {
			descriptors[i] = ObjectDescriptorFactory.make(parameterTypes[i], properties[i]
					.getPropertyName(), properties[i].isOptional());
		}
		return descriptors;
	}

	private Object callMethod(Method method, Object[] args) throws InvocationTargetException {
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
			// should not happen, is checked during formation of the array of actual arguments
			logError(method, e);
			return null;
		} catch (IllegalAccessException e) {
			// should not happen, is checked at the start of this method
			logError(method, e);
			return null;
		} finally {
			if (!wasAccessible)
				method.setAccessible(false);
		}
		return result;
	}

}
