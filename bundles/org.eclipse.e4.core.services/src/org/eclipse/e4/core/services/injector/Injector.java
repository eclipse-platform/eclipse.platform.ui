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
package org.eclipse.e4.core.services.injector;

import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.internal.context.ContextInjector;

/**
 * Injector implementation. NOTE: This is a preliminary form; this API will change.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
final public class Injector implements IDisposable {

	final private ContextInjector eInjector;

	public Injector(IObjectProvider context) {
		eInjector = new ContextInjector(context);
	}

	public void inject(Object object) {
		eInjector.inject(object);
	}

	public void uninject(Object object) {
		eInjector.uninject(object);
	}

	public void added(IObjectDescriptor descriptor) {
		eInjector.added(descriptor);
	}

	public void removed(IObjectDescriptor descriptor) {
		eInjector.removed(descriptor);
	}

	public Object make(Class clazz) {
		return eInjector.make(clazz);
	}

	public Object invoke(Object object, String methodName, Object defaultValue) {
		return eInjector.invoke(object, methodName, defaultValue);
	}

	public void injectStatic(Class clazz) {
		eInjector.injectStatic(clazz);
	}

	public void reparent(IObjectProvider oldParent) {
		eInjector.reparent(oldParent);
	}

	public void dispose() {
		eInjector.dispose();
	}

}
