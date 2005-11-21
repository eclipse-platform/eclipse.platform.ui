/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class which helps managing messages
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorMessages {

	private static ResourceBundle resourceBundle;

	/**
	 * Returns the resource bundle used by all classes in this Project
	 */
	public static ResourceBundle getResourceBundle() {
		try {
			return ResourceBundle.getBundle("messages");//$NON-NLS-1$
		} catch (MissingResourceException e) {
			// does nothing - this method will return null and
			// getString(String) will return the key
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

	public static String getString(String key) {
		if (resourceBundle == null) {
			resourceBundle = getResourceBundle();
		}

		if (resourceBundle != null) {
			try {
				return resourceBundle.getString(key);
			} catch (MissingResourceException e) {
				return "!" + key + "!";//$NON-NLS-2$//$NON-NLS-1$
			}
		}
		return "!" + key + "!";//$NON-NLS-2$//$NON-NLS-1$ 
	}

	public static String getString(String key, Object[] args) {

		try {
			return MessageFormat.format(getString(key), args);
		} catch (IllegalArgumentException e) {
			return getString(key);
		}

	}

	public static String getString(String key, Object[] args, int x) {

		return getString(key);
	}

}
