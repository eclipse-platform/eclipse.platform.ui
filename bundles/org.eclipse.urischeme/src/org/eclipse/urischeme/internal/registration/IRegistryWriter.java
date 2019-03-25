/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

/**
 * Interface having all the static methods and also methods which perform
 * actions in the windows registry.<br />
 * This interface is predominantly used by WindowsRegistry.
 *
 */
public interface IRegistryWriter {

	/**
	 * @param string String to be formatted
	 * @return returns the formatted String
	 */
	static String quote(String string) {
		return String.format("\"%s\"", string); //$NON-NLS-1$
	}

	/**
	 * Adds scheme and handler path to the OS.
	 *
	 * @param scheme The uri scheme which should be handled by the application
	 *               mentioned in the OS.
	 * @param launcherPath The path to the launcher binary
	 * @throws WinRegistryException if Windows Registry I/O error occurred
	 *
	 */
	void addScheme(String scheme, String launcherPath) throws WinRegistryException;


	/**
	 * Removes the scheme and handler path from the OS.
	 *
	 * @param scheme The uri scheme which should not be handled anymore by the
	 *               application mentioned in the OS.
	 *
	 * @throws WinRegistryException if Windows Registry I/O error occurred
	 *
	 */
	void removeScheme(String scheme) throws WinRegistryException;

	/**
	 * @param scheme Scheme for which the handler path is required
	 * @return returns the absolute path
	 * @throws WinRegistryException if Windows Registry I/O error occurred
	 */
	String getRegisteredHandlerPath(String scheme) throws WinRegistryException;

}
