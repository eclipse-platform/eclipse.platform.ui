/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ibm.icu.text.MessageFormat;


/**
 * Helper class to get NLSed messages.
 *
 * @since 3.4
 */
class TextMessages {
	private static final String BUNDLE_NAME= "org.eclipse.jface.text.TextMessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE= ResourceBundle.getBundle(BUNDLE_NAME);

	private TextMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static String getFormattedString(String key, Object arg) {
		return getFormattedString(key, new Object[] { arg });
	}

	public static String getFormattedString(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}

}
