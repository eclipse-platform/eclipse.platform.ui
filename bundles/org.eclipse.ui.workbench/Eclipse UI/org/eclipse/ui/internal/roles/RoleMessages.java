/*
 * Created on Sep 19, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.internal.roles;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author tod
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RoleMessages {

	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.roles.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * Create the role messages
	 */
	private RoleMessages() {

	}
	/**
	 * Get the value of String.
	 * @param key
	 * @return String
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
