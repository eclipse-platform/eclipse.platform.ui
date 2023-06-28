/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

class JFaceTextMessages {

	private static final String RESOURCE_BUNDLE= "org.eclipse.jface.text.JFaceTextMessages";//$NON-NLS-1$

	private static ResourceBundle fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);

	private JFaceTextMessages() {
	}

	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}
}
