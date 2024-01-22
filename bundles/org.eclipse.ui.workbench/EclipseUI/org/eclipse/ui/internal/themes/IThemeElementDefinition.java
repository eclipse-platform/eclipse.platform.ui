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
 * A basic element (color, font) of a theme must implement this interface.
 *
 * @since 3.0
 */
public interface IThemeElementDefinition {

	/**
	 * Returns the name for this element.
	 *
	 * @return the name for this element
	 */
	String getName();

	/**
	 * Returns the id for this element.
	 *
	 * @return the id for this element. This will never be <code>null</code>.
	 */
	String getId();

	/**
	 * Returns the description for this element.
	 *
	 * @return the description for this element. This may be <code>null</code>.
	 */
	String getDescription();

}
