/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @since 3.1
 */
public class MessageResourceBundle {

	private static final String EXTENSION = ".properties"; //$NON-NLS-1$

	/*
	 * Load the given resource bundle using the specified class loader. 
	 */
	public static Properties load(String bundleName, ClassLoader loader) {
		Properties result = new Properties();
		String[] variants = buildVariants(bundleName);
		// search the dirs in reverse order so the cascading defaults is set correctly
		for (int i = variants.length - 1; i > -1; i--) {
			String name = variants[i];
			InputStream input = loader.getResourceAsStream(name);
			if (input == null)
				continue;
			result = new Properties(result);
			try {
				result.load(input);
			} catch (IOException e) {
				// TODO log
			} finally {
				if (input != null)
					try {
						input.close();
					} catch (IOException e) {
						// ignore
					}
			}
		}
		return result;
	}

	/*
	 * Build an array of directories to search
	 */
	private static String[] buildVariants(String root) {
		String nl = Locale.getDefault().toString();
		ArrayList result = new ArrayList();
		root = root.replace('.', '/');
		int lastSeparator;
		while ((lastSeparator = nl.lastIndexOf('_')) != -1) {
			result.add(root + '_' + nl + EXTENSION);
			if (lastSeparator != -1)
				nl = nl.substring(0, lastSeparator);
		}
		result.add(root + '_' + nl + EXTENSION);
		// always add entry for the default locale string
		result.add(root + EXTENSION);
		return (String[]) result.toArray(new String[result.size()]);
	}

}
