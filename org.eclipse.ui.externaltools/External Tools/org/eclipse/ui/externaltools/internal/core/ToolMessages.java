package org.eclipse.ui.externaltools.internal.core;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class which helps managing messages
 */
public class ToolMessages {
	private static final String RESOURCE_BUNDLE= "org.eclipse.ui.externaltools.internal.core.messages"; //$NON-NLS-1$
	private static ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
	
	private ToolMessages(){
		// prevent instantiation of class
	}
	
	/**
	 * Returns the formatted message for the given key in
	 * the resource bundle. 
	 *
	 * @param key the message name
	 * @param args the message arguments
	 * @return the formatted message
	 */	
	public static String format(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}
	
	/**
	 * Returns the message with the given key in
	 * the resource bundle. If there isn't any value under
	 * the given key, the key is returned.
	 *
	 * @param key the message name
	 * @return the message
	 */	
	public static String getString(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
	
	/**
	 * Returns the resource bundle for the plug-in
	 */
	public static ResourceBundle getResourceBundle() {
		return bundle;
	}
}
