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
package org.eclipse.e4.core.di;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.eclipse.e4.core.internal.di.ObjectDescriptor;

/**
 * NOTE: This is a preliminary form; this API will change.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
final public class ObjectDescriptorFactory {

	private ObjectDescriptorFactory() {
		// prevents instantiation
	}

	static public IObjectDescriptor make(Type type, Annotation[] annotations) {
		return new ObjectDescriptor(type, annotations);
	}
}
