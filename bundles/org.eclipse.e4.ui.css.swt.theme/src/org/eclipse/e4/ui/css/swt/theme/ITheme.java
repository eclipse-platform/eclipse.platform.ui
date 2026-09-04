/*******************************************************************************
 * Copyright (c) 2010 Tom Schindl and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.theme;

/**
 * A theme which is composed of stylesheets and resources
 */
public interface ITheme {
	/**
	 * @return the theme id
	 */
	String getId();

	/**
	 * @return the label
	 */
	String getLabel();

	/**
	 * Whether this theme is meant to be used with a dark appearance, as declared by
	 * the <code>isDarkTheme</code> attribute of the theme extension. Implementations
	 * that do not override this keep the id based classification the platform used
	 * before the attribute existed.
	 */
	default boolean isDark() {
		String id = getId();
		return id != null && id.contains("dark"); //$NON-NLS-1$
	}
}
