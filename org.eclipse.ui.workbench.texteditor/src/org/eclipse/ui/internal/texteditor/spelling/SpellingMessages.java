/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.spelling;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Spelling messages. Helper class to get NLSed messages.
 * 
 * @since 3.1
 */
class SpellingMessages {

	/** Resource bundle base name */
	private static final String BUNDLE_NAME= SpellingMessages.class.getName();

	/** Resource bundle */
	private static final ResourceBundle RESOURCE_BUNDLE= ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * Gets a string from the resource bundle.
	 * 
	 * @param key the string used to get the bundle value, must not be <code>null</code>
	 * @return the string from the resource bundle
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Not for instantiation.
	 */
	private SpellingMessages() {
	}
}
