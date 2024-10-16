/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.themes;

/**
 * A theme element that may belong to a category.
 *
 * @since 3.0
 */
public interface ICategorizedThemeElementDefinition extends IThemeElementDefinition {

	/**
	 * Returns the category of this element.
	 *
	 * @return the category of this element, or <code>null</code> if it does not
	 *         belong to one.
	 */
	String getCategoryId();
}
