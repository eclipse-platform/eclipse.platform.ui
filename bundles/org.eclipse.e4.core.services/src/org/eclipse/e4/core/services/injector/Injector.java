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

import org.eclipse.e4.core.services.internal.context.ContextInjector;

/**
 * Injector implementation. NOTE: This is a preliminary form; this API will change.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
final public class Injector {

	final private ContextInjector eInjector;

	public Injector(IObjectProvider context) {
		eInjector = new ContextInjector(context);
	}

	// TBD change name to key
	public void inject(String name, Object userObject) {
		eInjector.inject(name, userObject);
	}

	public void inject(Object userObject) {
		eInjector.inject(userObject);
	}

	public void injectStatic(Class clazz) {
		eInjector.injectStatic(clazz);
	}

	// TBD change name to key
	public void uninject(String name, Object userObject) {
		eInjector.uninject(name, userObject);
	}

	public void uninject(Object userObject) {
		eInjector.uninject(userObject);
	}

	public Object make(Class clazz) {
		return eInjector.make(clazz);
	}

}
