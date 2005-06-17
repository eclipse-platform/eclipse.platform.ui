/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.quickdiff;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @since 3.0
 */
class QuickDiffMessages {

	private static final String BUNDLE_NAME= "org.eclipse.ui.internal.editors.quickdiff.QuickDiffMessages";//$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE= ResourceBundle.getBundle(BUNDLE_NAME);

	private QuickDiffMessages() {
	}

	public static ResourceBundle getResourceBundle() {
		return RESOURCE_BUNDLE;
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
