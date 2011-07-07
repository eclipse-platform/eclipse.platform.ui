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

import org.eclipse.e4.core.internal.di.InjectorImpl;

/**
 * Use this class to obtain an instance of the dependency injector.
 * 
 * @noextend This class is not intended to be extended by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
final public class InjectorFactory {

	private static IInjector injector = new InjectorImpl();

	/**
	 * Hides constructor to prevents instantiation.
	 */
	private InjectorFactory() {
		// prevents instantiation
	}

	/**
	 * Returns default instance of the dependency injector.
	 * @return default dependency injector
	 */
	public static IInjector getDefault() {
		return injector;
	}

	/**
	 * Returns a new instance of the dependency injector.
	 * @return a new instance of the dependency injector
	 */
	public static IInjector makeInjector() {
		return new InjectorImpl();
	}
}
