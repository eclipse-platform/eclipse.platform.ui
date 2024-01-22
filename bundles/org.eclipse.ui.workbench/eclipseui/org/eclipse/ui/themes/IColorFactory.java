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
package org.eclipse.ui.themes;

import org.eclipse.swt.graphics.RGB;

/**
 * A factory interface that may be used to specify a color value. This is
 * (optionally) used by the themes extension point for color value definitions.
 * <p>
 * Example usage:
 * </p>
 * <code>
 * &lt;colorDefinition
 *     label="Custom Color"
 *     id="example.customColor"
 * 	   colorFactory="some.implementor.of.IColorFactory"&gt;
 * &lt;/colorDefinition&gt;
 * </code>
 *
 * @since 3.0
 */
public interface IColorFactory {

	/**
	 * Create a new color.
	 *
	 * @return a new color. This must never be <code>null</code>.
	 */
	RGB createColor();
}
