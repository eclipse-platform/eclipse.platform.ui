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
package org.eclipse.e4.core.services.osgi;

/**
 * A service that allows clients to publish and retrieve simple names for services. This
 * is suitable for service injection so that client fields and methods can be simple names.
 */
public interface IServiceAliasRegistry {
	public static final String SERVICE_NAME = IServiceAliasRegistry.class.getName();

	/**
	 * Returns the service alias associated with the given qualified service name. If the registry
	 * does not contain an alias for the qualified service name, the service name is returned.
	 * @param serviceName The qualified service name
	 * @return The service alias, or the provided service name
	 */
	public String findAlias(String serviceName);

	/**
	 * Registers an alias for a given service class name.
	 * @param alias The service alias
	 * @param clazz The fully qualified service class name
	 */
	public void registerAlias(String alias, String clazz);

	/**
	 * Returns the service class name associated with the given alias. If the registry
	 * does not contain a qualified service name for the alias, the alias itself is returned.
	 * @param alias The service alias
	 * @return The qualified service class name, or the provided alias
	 */
	public String resolveAlias(String alias);

	/**
	 * Unregisters an alias for a given service class name
	 * @param alias The service alias
	 */
	public void unregisterAlias(String alias);

}
