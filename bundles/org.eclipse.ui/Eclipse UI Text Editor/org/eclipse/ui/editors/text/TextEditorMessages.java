/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.ui.editors.text;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

class TextEditorMessages {

	private static final String RESOURCE_BUNDLE = "org.eclipse.ui.editors.text.TextEditorMessages"; //$NON-NLS-1$

	private static ResourceBundle fgResourceBundle =
		ResourceBundle.getBundle(RESOURCE_BUNDLE);

	private TextEditorMessages() {
	}

	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}

	/**
	* Returns the formatted message for the given key in
	* the resource bundle. 
	*
	* @param key the resource name
	* @param args the message arguments
	* @return the string
	*/
	public static String format(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}

}
