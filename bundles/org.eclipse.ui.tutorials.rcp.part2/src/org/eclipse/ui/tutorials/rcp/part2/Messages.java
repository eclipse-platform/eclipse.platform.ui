/*
 * Created on Dec 31, 2003
 */
package org.eclipse.ui.tutorials.rcp.part2;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author sasebb
 */
public class Messages {

	private static final String BUNDLE_NAME = "org.eclipse.ui.tutorials.rcp.part2.RcpTutorial"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * 
	 */
	private Messages() {

		// TODO Auto-generated constructor stub
	}
	/**
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		// TODO Auto-generated method stub
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
