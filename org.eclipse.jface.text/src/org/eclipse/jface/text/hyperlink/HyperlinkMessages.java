/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.hyperlink;

import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Helper class to get NLSed messages.
 *
 * @since 3.4
 */
class HyperlinkMessages {
	private static final String BUNDLE_NAME= HyperlinkMessages.class.getName();

	private static final ResourceBundle RESOURCE_BUNDLE= ResourceBundle.getBundle(BUNDLE_NAME);

	private HyperlinkMessages() {
	}

	/**
	 * Gets a string from the resource bundle.
	 *
	 * @param key the string used to get the bundle value, must not be
	 *            <code>null</code>
	 * @return the string from the resource bundle
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
