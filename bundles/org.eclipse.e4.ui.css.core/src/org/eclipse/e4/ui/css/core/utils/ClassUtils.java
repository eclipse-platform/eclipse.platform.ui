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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class utils.
 */
public class ClassUtils {

	private static final Map<Class<?>, String> simpleNames = new ConcurrentHashMap<>();
	/**
	 * Return a simple name of Class <code>c</code>. For inner classes, the hyphen
	 * is used, e.g., for Outer$Inner, the return value is "Outer-Inner"
	 */
	public static String getSimpleName(Class<?> c) {
		return simpleNames.computeIfAbsent(c, ClassUtils::computeSimpleName);
	}

	private static String computeSimpleName(Class<?> c) {
		String name = c.getName();
		int index = name.lastIndexOf('.');
		if (index > 0) {
			name = name.substring(index + 1, name.length());
		}

		return name.replace('$', '-').intern();
	}

	/**
	 * Return the package name of Class <code>c</code>.
	 */
	public static String getPackageName(Class<?> c) {
		Package p = c.getPackage();
		return p == null ? null : p.getName();
	}
}
