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
package org.eclipse.e4.core.services.internal.context;

import org.eclipse.e4.core.services.injector.IObjectDescriptor;

public class ContextObjectDescriptor implements IObjectDescriptor {

	final private String propertyToInject;
	final private Class elementClass;

	public ContextObjectDescriptor(String propertyToInject, Class elementClass) {
		this.propertyToInject = propertyToInject;
		this.elementClass = elementClass;
	}

	public String getPropertyName() {
		return propertyToInject;
	}

	public Class getElementClass() {
		return elementClass;
	}

	public String getKey() {
		String result = getPropertyName();
		if (result != null)
			return result;
		Class elementClass = getElementClass();
		if (elementClass != null)
			return elementClass.getName();
		return null;
	}

}
