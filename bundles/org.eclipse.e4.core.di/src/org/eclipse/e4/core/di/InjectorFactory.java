/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 441742
 *******************************************************************************/
package org.eclipse.e4.core.di;

import org.eclipse.e4.core.internal.di.InjectorImpl;

/**
 * Use this class to obtain an instance of the dependency injector.
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 1.7
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
