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
package org.eclipse.e4.core.services.injector;

import java.lang.reflect.Type;
import javax.inject.Named;
import org.eclipse.e4.core.services.internal.context.ObjectDescriptor;

/**
 * NOTE: This is a preliminary form; this API will change.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
final public class ObjectDescriptorFactory {

	static final private String named = Named.class.getName();

	private ObjectDescriptorFactory() {
		// prevents instantiation
	}

	static public IObjectDescriptor make(Class<?> clazz, boolean optional) {
		return new ObjectDescriptor(clazz, null, null, optional);
	}

	static public IObjectDescriptor make(Type type, boolean optional) {
		return new ObjectDescriptor(type, null, null, optional);
	}

	static public IObjectDescriptor make(String name, boolean optional) {
		return new ObjectDescriptor(null, new String[] { named }, new String[] { name }, optional);
	}

	static public IObjectDescriptor make(Class<?> clazz, String name, boolean optional) {
		if (name == null)
			return make(clazz, optional);
		return new ObjectDescriptor(clazz, new String[] { named }, new String[] { name }, optional);
	}

	static public IObjectDescriptor make(Type type, String name, boolean optional) {
		if (name == null)
			return make(type, optional);
		return new ObjectDescriptor(type, new String[] { named }, new String[] { name }, optional);
	}
}
