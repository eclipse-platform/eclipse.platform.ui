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


/**
 * Interface for the Theme descriptors
 *
 * @since 3.0
 */
public interface IThemeDescriptor extends IThemeElementDefinition{
	public static final String TAB_BORDER_STYLE = "TAB_BORDER_STYLE";	 //$NON-NLS-1$

	public static final String TAB_TITLE_FONT = "TAB_TITLE_FONT"; //$NON-NLS-1$
	public static final String TAB_TITLE_TEXT_COLOR_ACTIVE = "TAB_TITLE_TEXT_COLOR_ACTIVE"; //$NON-NLS-1$
	public static final String TAB_TITLE_TEXT_COLOR_DEACTIVATED = "TAB_TITLE_TEXT_COLOR_DEACTIVATED"; //$NON-NLS-1$
	public static final String TAB_TITLE_TEXT_COLOR_HOVER = "TAB_TITLE_TEXT_COLOR_HOVER"; //$NON-NLS-1$
	public static final String VIEW_BORDER_STYLE = "VIEW_BORDER_STYLE"; //$NON-NLS-1$
	public static final String VIEW_TITLE_FONT = "VIEW_TITLE_FONT"; //$NON-NLS-1$
	public static final String VIEW_TITLE_GRADIENT_COLOR_ACTIVE = "VIEW_TITLE_GRADIENT_COLOR_ACTIVE"; //$NON-NLS-1$
	public static final String VIEW_TITLE_GRADIENT_COLOR_DEACTIVATED = "VIEW_TITLE_GRADIENT_COLOR_DEACTIVATED"; //$NON-NLS-1$
	
	public static final String VIEW_TITLE_GRADIENT_COLOR_NORMAL = "VIEW_TITLE_GRADIENT_COLOR_NORMAL"; //$NON-NLS-1$
	public static final String VIEW_TITLE_GRADIENT_DIRECTION = "VIEW_TITLE_GRADIENT_DIRECTION"; //$NON-NLS-1$
	public static final String VIEW_TITLE_GRADIENT_PERCENTS_ACTIVE = "VIEW_TITLE_GRADIENT_PERCENTS_ACTIVE"; //$NON-NLS-1$
	public static final String VIEW_TITLE_GRADIENT_PERCENTS_DEACTIVATED = "VIEW_TITLE_GRADIENT_PERCENTS_DEACTIVATED"; //$NON-NLS-1$
	public static final String VIEW_TITLE_GRADIENT_PERCENTS_NORMAL = "VIEW_TITLE_GRADIENT_PERCENTS_NORMAL"; //$NON-NLS-1$
	public static final String VIEW_TITLE_TEXT_COLOR_ACTIVE = "VIEW_TITLE_TEXT_COLOR_ACTIVE"; //$NON-NLS-1$
	public static final String VIEW_TITLE_TEXT_COLOR_DEACTIVATED = "VIEW_TITLE_TEXT_COLOR_DEACTIVATED"; //$NON-NLS-1$
	public static final String VIEW_TITLE_TEXT_COLOR_NORMAL = "VIEW_TITLE_TEXT_COLOR_NORMAL"; //$NON-NLS-1$
	
	/**
	 * Returns the color overrides for this theme.
	 * @return ColorDefinition []
	 */
	public ColorDefinition [] getColors();
	
	/**
	 * Returns the font overrides for this theme.
	 * @return GradientDefinition []
	 */
	public FontDefinition [] getFonts();
	
	/**
	 * Returns the gradient overrides for this theme.
	 * @return GradientDefinition []
	 */
	public GradientDefinition [] getGradients();	
	
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
