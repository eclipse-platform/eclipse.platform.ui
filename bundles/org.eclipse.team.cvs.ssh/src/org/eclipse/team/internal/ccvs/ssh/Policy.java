package org.eclipse.team.internal.ccvs.ssh;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Policy {
	protected static ResourceBundle bundle = null;

	/**
	 * Creates a NLS catalog for the given locale.
	 */
	public static void localize(String bundleName) {
		bundle = ResourceBundle.getBundle(bundleName);
	}
	
	/**
	 * Gets a string from the resource bundle. We don't want to crash because of a missing String.
	 * Returns the key if not found.
	 */
	public static String bind(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!"; //$NON-NLS-1$  //$NON-NLS-2$
		}
	}
	
	/**
	 * Gets a string from the resource bundle and binds it with the given arguments. If the key is 
	 * not found, return the key.
	 */
	public static String bind(String key, Object[] args) {
		try {
			return MessageFormat.format(bind(key), args);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";  //$NON-NLS-1$  //$NON-NLS-2$
		}
	}

}