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

/**
 * Describes binding between object description and its implementation to be used by 
 * the dependency injection.
 * @see IInjector#addBinding(Class)
 * @see IInjector#addBinding(IBinding)
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IBinding {

	/**
	 * Creates a binding for the specified name.
	 * @param name name for this binding
	 * @return binding for the specified name
	 */
	public IBinding named(String name);

	/**
	 * Creates a binding for the specified class
	 * @param clazz class for this binding
	 * @return binding for the specified class
	 */
	public IBinding implementedBy(Class<?> clazz);
}
