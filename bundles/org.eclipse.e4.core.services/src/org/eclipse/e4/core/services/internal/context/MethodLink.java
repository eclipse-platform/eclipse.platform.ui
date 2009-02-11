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

import java.lang.reflect.Method;

import org.eclipse.e4.core.services.context.IEclipseContext;

public class MethodLink extends ObjectLink {
	
	private Method method;	// TBD this should be wrapped into a WeakReference but then class gets unloaded 
							// due to the current part implementation
	
	public MethodLink(Object userObject, IEclipseContext context, String candidateName, Method method) {
		super(userObject, context, candidateName);
		this.method = method;
	}
	
	public void run() {
//		Method method = (Method) methodRef.get();
//		if (method == null)
//			return;
		String key = findKey(candidateName);
		if (key == null) { // value not set in the context
			if (isSet) { // value has been removed from the context
				setMethod(method, null);
				isSet = false;
			}
			return;
		}
		Class[] parameterTypes = method.getParameterTypes();
		Object value = context.get(key,parameterTypes);
		isSet = setMethod(method, value);
	}

}
