/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.databinding;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ibm.icu.text.MessageFormat;

/**
 * @since 1.0
 * 
 */
public class BindingMessages {

	/**
	 * The Binding resource bundle; eagerly initialized.
	 */
	private static final ResourceBundle bundle = ResourceBundle
			.getBundle("org.eclipse.core.internal.databinding.messages"); //$NON-NLS-1$

	/**
	 * Key to be used for an index out of range message.
	 */
	public static final String INDEX_OUT_OF_RANGE = "IndexOutOfRange"; //$NON-NLS-1$

	/**
	 * Key to be used for a "Multiple Problems." message.
	 */
	public static final String MULTIPLE_PROBLEMS = "MultipleProblems"; //$NON-NLS-1$

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

	/**
	 * Returns a formatted string with the given key in the resource bundle for
	 * JFace Data Binding.
	 * 
	 * @param key
	 * @param arguments
	 * @return formatted string, the key if the key is invalid
	 */
	public static String formatString(String key, Object[] arguments) {
		try {
			return MessageFormat.format(bundle.getString(key), arguments);
		} catch (MissingResourceException e) {
			return key;
		}
	}
}
