/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import org.eclipse.ui.internal.presentation.ColorDefinition;

/**
 * Interface for the Theme descriptors
 *
 * @since 3.0
 */
public interface IThemeDescriptor {
	
	public static final String VIEW_TITLE_GRADIENT_COLOR_NORMAL = "VIEW_TITLE_GRADIENT_COLOR_NORMAL";
	public static final String VIEW_TITLE_GRADIENT_COLOR_ACTIVE = "VIEW_TITLE_GRADIENT_COLOR_ACTIVE";
	public static final String VIEW_TITLE_GRADIENT_COLOR_DEACTIVATED = "VIEW_TITLE_GRADIENT_COLOR_DEACTIVATED";
	public static final String VIEW_TITLE_GRADIENT_PERCENTS_NORMAL = "VIEW_TITLE_GRADIENT_PERCENTS_NORMAL";
	public static final String VIEW_TITLE_GRADIENT_PERCENTS_ACTIVE = "VIEW_TITLE_GRADIENT_PERCENTS_ACTIVE";
	public static final String VIEW_TITLE_GRADIENT_PERCENTS_DEACTIVATED = "VIEW_TITLE_GRADIENT_PERCENTS_DEACTIVATED";
	public static final String VIEW_TITLE_GRADIENT_DIRECTION = "VIEW_TITLE_GRADIENT_DIRECTION";
	public static final String VIEW_TITLE_TEXT_COLOR_NORMAL = "VIEW_TITLE_TEXT_COLOR_NORMAL";
	public static final String VIEW_TITLE_TEXT_COLOR_ACTIVE = "VIEW_TITLE_TEXT_COLOR_ACTIVE";
	public static final String VIEW_TITLE_TEXT_COLOR_DEACTIVATED = "VIEW_TITLE_TEXT_COLOR_DEACTIVATED";
	public static final String VIEW_TITLE_FONT = "VIEW_TITLE_FONT";
	public static final String VIEW_BORDER_STYLE = "VIEW_BORDER_STYLE";

	public static final String TAB_TITLE_FONT = "TAB_TITLE_FONT";
	public static final String TAB_TITLE_TEXT_COLOR_HOVER = "TAB_TITLE_TEXT_COLOR_HOVER";
	public static final String TAB_TITLE_TEXT_COLOR_ACTIVE = "TAB_TITLE_TEXT_COLOR_ACTIVE";
	public static final String TAB_TITLE_TEXT_COLOR_DEACTIVATED = "TAB_TITLE_TEXT_COLOR_DEACTIVATED";
	public static final String TAB_BORDER_STYLE = "TAB_BORDER_STYLE";	
	/**
	 * Returns the id of the Theme.
	 * @return String
	 */
	public String getID();
	
	/**
	 * Returns the name of the Theme.
	 * @return String
	 */
	public String getName();

	/**
	 * Returns the color overrides for this theme.
	 * @return ColorDefinition []
	 */
	public ColorDefinition [] getColorOverrides();
	
	/**
	 * Returns the descriptor of the tab theme.
	 * @return ITabThemeDescriptor
	 */
	public ITabThemeDescriptor getTabThemeDescriptor();

	/**
	 * Returns the descriptor of the view theme.
	 * @return IViewThemeDesc
	 */	
	public IViewThemeDescriptor getViewThemeDescriptor();
}
