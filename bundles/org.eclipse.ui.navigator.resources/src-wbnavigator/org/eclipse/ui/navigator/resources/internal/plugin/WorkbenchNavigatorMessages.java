/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.resources.internal.plugin;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class which helps managing messages
 */
public class WorkbenchNavigatorMessages {
	private static final String RESOURCE_BUNDLE = "wbnavigator";//$NON-NLS-1$
	private static ResourceBundle bundle;

	private WorkbenchNavigatorMessages() {
		// prevent instantiation of class
	}


	/**
	 * Returns the resource bundle used by all classes in this Project
	 */
	public static ResourceBundle getResourceBundle() {
		try {
			return ResourceBundle.getBundle(RESOURCE_BUNDLE);//$NON-NLS-1$
		} catch (MissingResourceException e) {
			// does nothing - this method will return null and
			// getString(String, String) will return the key
			// it was called with
		}
		return null;
	}

	/**
	 * Returns the formatted message for the given key in the resource bundle.
	 * 
	 * @param key
	 *            the resource name
	 * @param args
	 *            the message arguments
	 * @return the string
	 */
	public static String format(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}

	/**
	 * Returns the resource object with the given key in the resource bundle. If there isn't any
	 * value under the given key, the key is returned.
	 * 
	 * @param key
	 *            the resource name
	 * @return the string
	 */
	public static String getString(String key) {
		if (bundle == null)
			bundle = getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the resource object with the given key in the resource bundle. If there isn't any
	 * value under the given key, the default value is returned.
	 * 
	 * @param key
	 *            the resource name
	 * @param def
	 *            the default value
	 * @return the string
	 */
	public static String getString(String key, String def) {
		if (bundle == null)
			bundle = getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return def;
		}
	}
}