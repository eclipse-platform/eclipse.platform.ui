/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.ui.examples.multipageeditor;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Retrieves message strings from the resource bundle.
 */
/* package */ class MPEMessages {


	private static final String RESOURCE_BUNDLE= "org.eclipse.ui.examples.multipageeditor.messages";//$NON-NLS-1$


	private static ResourceBundle fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);


	private MPEMessages() {
	}

	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}
}
