/*******************************************************************************
* Copyright (c) 2018 SAP SE and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     SAP SE - initial API and implementation
*******************************************************************************/
package org.eclipse.urischeme.internal.registration;

/**
 * Wraps Windows Registry to read and write values. Can only be used for Keys
 * below HKEY_CURRENT_USER. The Windows Registry code has been tested in JAVA
 * 8,Java 9 and Java 10 versions
 *
 */
public interface IWinRegistry {

	/**
	 * Set a String value for an existing Key-Attribute. If the Key or the Attribute
	 * does not exist, then they are created. Note that if an existing Key-Attribute
	 * of some other type exists, it will be overwritten, with the new String value.
	 *
	 * @param key       below HKEY_CURRENT_USER
	 * @param attribute
	 * @param value
	 * @throws WinRegistryException
	 */
	void setValueForKey(String key, String attribute, String value) throws WinRegistryException;

	/**
	 * Reads Key-Attribute value of type string. Returns null if the key or the
	 * attribute do not exist, or if the type of the attribute is not String.
	 *
	 * @param key       below HKEY_CURRENT_USER
	 * @param attribute
	 * @return attribute value
	 * @throws WinRegistryException
	 */
	String getValueForKey(String key, String attribute) throws WinRegistryException;

	/**
	 * Removes the given key from the registry
	 *
	 * @param key below HKEY_CURRENT_USER
	 * @throws WinRegistryException in case of failures
	 */
	void deleteKey(String key) throws WinRegistryException;

}
