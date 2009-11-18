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

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.injector.IObjectDescriptor;
import org.eclipse.e4.core.services.injector.IObjectProvider;
import org.eclipse.e4.core.services.injector.Injector;

public class ObjectProviderContext implements IObjectProvider {

	final static private String ECLIPSE_CONTEXT_NAME = IEclipseContext.class.getName();

	final private IEclipseContext context;

	public ObjectProviderContext(IEclipseContext context) {
		this.context = context;
	}

	public boolean containsKey(IObjectDescriptor properties) {
		String key = getKey(properties);
		if (key == null)
			return false;
		if (ECLIPSE_CONTEXT_NAME.equals(key))
			return (context != null);
		return context.containsKey(key);
	}

	public Object get(IObjectDescriptor properties) {
		String key = getKey(properties);
		if (key == null)
			return null;
		if (ECLIPSE_CONTEXT_NAME.equals(key))
			return context;
		return context.get(key);
	}

	public void setInjector(Injector injector) {
		// TODO Auto-generated method stub
	}

	public String getKey(IObjectDescriptor key) {
		String result = key.getPropertyName();
		if (result != null)
			return result;
		Class elementClass = key.getElementClass();
		if (elementClass != null)
			return elementClass.getName();
		return null;
	}

}
