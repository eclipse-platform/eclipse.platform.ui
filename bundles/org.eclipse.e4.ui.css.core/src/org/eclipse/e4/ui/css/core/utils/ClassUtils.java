/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.utils;

/**
 * Class utils.
 */
public class ClassUtils {

	/**
	 * @param c the class for what a simple name is to be returned
	 * @return a simple name of Class <code>c</code>. For inner classes, the hyphen
	 *         is used, e.g., for Outer$Inner, the return value is "Outer-Inner"
	 */
	public static String getSimpleName(Class<?> c) {
		String name = c.getName();
		int index = name.lastIndexOf('.');
		if (index > 0) {
			name = name.substring(index + 1, name.length());
		}

		return name.replace('$', '-').intern();
	}

	/**
	 * Return the package name of Class <code>c</code>.
	 *
	 * @param c the class for what a simple name is to be returned
	 * @return the package name, if this class represents an array type, a primitive
	 *         type or void, this method returns <code>null</code>.
	 */
	public static String getPackageName(Class<?> c) {
		String name = c.getName();
		int index = name.lastIndexOf('.');
		if (index > 0) {
			return name.substring(0, index).intern();
		}
		return null;
	}
}
