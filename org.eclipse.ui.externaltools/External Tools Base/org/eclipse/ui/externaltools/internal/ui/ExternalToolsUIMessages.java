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
package org.eclipse.ui.externaltools.internal.ui;


import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class ExternalToolsUIMessages {
	private static final String BUNDLE_NAME= "org.eclipse.ui.externaltools.internal.ui.ExternalToolsUIMessages"; //$NON-NLS-1$
	
	private static final ResourceBundle RESOURCE_BUNDLE =
			ResourceBundle.getBundle(BUNDLE_NAME);

	private ExternalToolsUIMessages(){
		// prevent instantiation of class
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
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}	
}
