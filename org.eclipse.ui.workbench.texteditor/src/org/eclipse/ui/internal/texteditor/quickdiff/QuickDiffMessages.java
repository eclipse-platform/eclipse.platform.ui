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
package org.eclipse.ui.internal.texteditor.quickdiff;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Helper class which wraps the specified resource bundle
 * and offers methods to access the bundle.
 * 
 * @since 3.0
 */
public class QuickDiffMessages {

	private static final String RESOURCE_BUNDLE= "org.eclipse.ui.internal.texteditor.quickdiff.QuickDiffMessages";//$NON-NLS-1$

	private static ResourceBundle fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);

	private QuickDiffMessages() {
	}

	public static String getString(String key) {
		try {
			return fgResourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}
	
	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}

	public static String getFormattedString(String key, Object arg) {
		return getFormattedString(key, new Object[] { arg });
	}
	
	public static String getFormattedString(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);	
	}	
	
}
