package org.eclipse.jface.binding.internal;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @since 3.2
 * 
 */
public class BindingMessages {

	/**
	 * The Binding resource bundle; eagerly initialized.
	 */
	private static final ResourceBundle bundle = ResourceBundle
			.getBundle("org.eclipse.jface.binding.internal.messages"); //$NON-NLS-1$

	/**
	 * Returns the resource object with the given key in the resource bundle for
	 * JFace Data Binding. If there isn't any value under the given key, the key
	 * is returned.
	 * 
	 * @param key
	 *            the resource name
	 * @return the string
	 */
	public static String getString(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
}
