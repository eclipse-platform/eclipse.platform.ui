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
package org.eclipse.ui.wizards.newresource;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class which helps managing messages
 */
class ResourceMessages {
	private static final String RESOURCE_BUNDLE= "org.eclipse.ui.wizards.newresource.messages";//$NON-NLS-1$
	private static ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);

private ResourceMessages(){
	// prevent instantiation of class
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
	return MessageFormat.format(getString(key),args);
}
/**
 * Returns the resource object with the given key in
 * the resource bundle. If there isn't any value under
 * the given key, the key is returned.
 *
 * @param key the resource name
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
