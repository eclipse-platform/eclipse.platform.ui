/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.core.internal.refresh;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The <code>AutorefreshMessages</code> class is a standard
 * Messages class.
 */
public class AutorefreshMessages {
	
	private static final String BUNDLE_NAME = "org.eclipse.core.internal.refresh.AutorefreshMessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(BUNDLE_NAME);

	private AutorefreshMessages() {
	}

	/**
	 * @param key the message key, not <code>null</code>
	 * @return a translated string, or if the key could not be found,
	 * the key wrapped in '!' characters.
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
