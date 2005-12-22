/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @since 3.2
 * 
 */
public class BindingMessages {

	/**
	 * The Binding resource bundle; eagerly initialized.
	 */
	private static final ResourceBundle bundle = ResourceBundle
			.getBundle("org.eclipse.jface.internal.databinding.messages"); //$NON-NLS-1$

	/**
	 * Returns the resource object with the given key in the resource bundle for
	 * JFace Data Binding. If there isn't any value under the given key, the key
	 * is returned.
	 * 
	 * @param key
	 *            the resource name
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
