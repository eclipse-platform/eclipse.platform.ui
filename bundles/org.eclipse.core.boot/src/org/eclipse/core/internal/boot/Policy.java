/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.boot;

import java.text.MessageFormat;
import java.util.*;

public class Policy {
	private static String bundleName = "org.eclipse.core.internal.boot.messages";//$NON-NLS-1$
	private static ResourceBundle bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());

/**
 * Lookup the message with the given ID in this catalog 
 */
public static String bind(String id) {
	return bind(id, (String[])null);
}
/**
 * Lookup the message with the given ID in this catalog and bind its
 * substitution locations with the given string.
 */
public static String bind(String id, String binding) {
	return bind(id, new String[] {binding});
}
/**
 * Lookup the message with the given ID in this catalog and bind its
 * substitution locations with the given strings.
 */
public static String bind(String id, String binding1, String binding2) {
	return bind(id, new String[] {binding1, binding2});
}

/**
 * Lookup the message with the given ID in this catalog and bind its
 * substitution locations with the given string values.
 */
public static String bind(String id, String[] bindings) {
	if (id == null)
		return "No message available";//$NON-NLS-1$
	String message = null;
	try {
		message = bundle.getString(id);
	} catch (MissingResourceException e) {
		// If we got an exception looking for the message, fail gracefully by just returning
		// the id we were looking for.  In most cases this is semi-informative so is not too bad.
		return "Missing message: " + id + " in: " + bundleName;//$NON-NLS-1$ //$NON-NLS-2$
	}
	if (bindings == null)
		return message;
	return MessageFormat.format(message, bindings);
}
/**
 * Print a debug message to the console. If the given boolean is <code>true</code> then
 * pre-pend the message with the current date.
 */
public static void debug(boolean includeDate, String message) {
	if (includeDate) 
		message = new Date(System.currentTimeMillis()).toString() + " - "+ message; //$NON-NLS-1$
	System.out.println(message);
}
}
