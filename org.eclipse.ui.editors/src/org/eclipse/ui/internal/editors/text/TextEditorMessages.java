/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Helper class which wraps the specified resource bundle
 * and offers methods to access the bundle.
 * 
 * @since 2.1
 */
class TextEditorMessages {

	private static final String RESOURCE_BUNDLE= "org.eclipse.ui.internal.editors.text.TextEditorMessages";//$NON-NLS-1$

	private static ResourceBundle fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);

	private TextEditorMessages() {
	}

	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}
	
	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}

	public static String getFormattedString(String key, String arg) {
		return getFormattedString(key, new String[] { arg });
	}
	
	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getString(key), args);	
	}
	
	/**
	 * Gets a string from the resource bundle and formats it with the given argument.
	 * 
	 * @param key the string used to get the bundle value, must not be <code>null</code>
	 * @param arg the argument used to format the string
	 * @return the formatted string
	 * @since 3.1
	 */
	public static String getFormattedString(String key, Object arg) {
		String format= null;
		try {
			format= fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
		if (arg == null)
			arg= ""; //$NON-NLS-1$
		return MessageFormat.format(format, new Object[] { arg });
	}
}
