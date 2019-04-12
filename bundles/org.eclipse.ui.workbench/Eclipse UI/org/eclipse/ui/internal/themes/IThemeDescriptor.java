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

import java.util.Map;

/**
 * Interface for the Theme descriptors
 *
 * @since 3.0
 */
public interface IThemeDescriptor extends IThemeElementDefinition {
	String TAB_BORDER_STYLE = "TAB_BORDER_STYLE"; //$NON-NLS-1$

	/**
	 * Returns the color overrides for this theme.
	 * 
	 * @return ColorDefinition []
	 */
	ColorDefinition[] getColors();

	/**
	 * Returns the font overrides for this theme.
	 * 
	 * @return GradientDefinition []
	 */
	FontDefinition[] getFonts();

	/**
	 * Returns the data map for this theme.
	 *
	 * @return the data map. This will be read only.
	 */
	Map getData();
}
