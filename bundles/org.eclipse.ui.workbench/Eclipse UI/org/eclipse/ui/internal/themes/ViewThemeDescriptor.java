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

import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import org.eclipse.jface.resource.StringConverter;

/**
 * Theme descriptor for views.
 *
 * @since 3.0
 */
public class ViewThemeDescriptor implements IViewThemeDescriptor {
	
	private static final String ATT_BORDER_STYLE = "borderStyle";//$NON-NLS-1$
	private static final String TAG_TITLE_INFO = "titlebar";//$NON-NLS-1$
	private static final String ATT_TITLE_NORMAL_BG_COLOR = "normalBGColor";//$NON-NLS-1$
	private static final String ATT_TITLE_ACTIVE_BG_COLOR = "activeBGColor";//$NON-NLS-1$
	private static final String ATT_TITLE_DEACTIVATED_BG_COLOR = "normalBGColor";//$NON-NLS-1$
	private static final String ATT_TITLE_NORMAL_BG_PERCENTS = "normalBGPercents";//$NON-NLS-1$
	private static final String ATT_TITLE_ACTIVE_BG_PERCENTS = "activeBGPercents";//$NON-NLS-1$
	private static final String ATT_TITLE_DEACTIVATED_BG_PERCENTS = "normalBGPercents";//$NON-NLS-1$
	private static final String ATT_TITLE_NORMAL_TEXT_COLOR = "normalTextColor";//$NON-NLS-1$
	private static final String ATT_TITLE_ACTIVE_TEXT_COLOR = "activeTextColor";//$NON-NLS-1$
	private static final String ATT_TITLE_DEACTIVATED_TEXT_COLOR = "deactivatedTextColor";//$NON-NLS-1$
	private static final String ATT_TITLE_GRADIENT_DIRECTION = "gradientDirection";//$NON-NLS-1$
	private static final String ATT_TITLE_FONT = "font";//$NON-NLS-1$
		
	private IConfigurationElement configElement;
	private int borderStyle;
	private String normalTextColor;
	private String activeTextColor;
	private String deactivatedTextColor;
	private String [] normalColors;
	private String [] activeColors;
	private String [] deactivatedColors;
	private int [] normalGradPercents;
	private int [] activeGradPercents;
	private int [] deactivatedGradPercents;
	private int gradientDirection;
	private String titleFont;
	
	public ViewThemeDescriptor (IConfigurationElement element) throws CoreException {
		configElement = element;
		borderStyle = SWT.BORDER;
		processExtension();
	}
	
	public int [] getGradientPercents (String key) {
		int result[] = this.normalGradPercents;
		if (key == IThemeDescriptor.VIEW_TITLE_GRADIENT_COLOR_ACTIVE)
			result = activeGradPercents;
		else if (key == IThemeDescriptor.VIEW_TITLE_GRADIENT_COLOR_DEACTIVATED)
			result = deactivatedGradPercents;
		return result;
	}
	
	public int getGradientDirection (String key) {
		return gradientDirection;
	}
	
	public Color [] getGradientColors (String key) {
		String result[] = normalColors;
		if (key == IThemeDescriptor.VIEW_TITLE_GRADIENT_COLOR_ACTIVE)
			result = activeColors;
		else if (key == IThemeDescriptor.VIEW_TITLE_GRADIENT_COLOR_DEACTIVATED)
			result = deactivatedColors;
		return createColors(result);
	}
	
	public Color getColor (String key) {
		String result = normalTextColor;
		if (key == IThemeDescriptor.VIEW_TITLE_TEXT_COLOR_ACTIVE)
			result = activeTextColor;
		else if (key == IThemeDescriptor.VIEW_TITLE_TEXT_COLOR_DEACTIVATED)
			result = deactivatedTextColor;
		return new Color(null, StringConverter.asRGB(result));
	}
	
	public Font  getFont (String key) {
		if (titleFont == null)
			return null;		
		return new Font(null, StringConverter.asFontData(titleFont));
	}
	
	public int  getBorderStyle () {
		return borderStyle;
	}
	
	/*
	 * load a TabThemeDescriptor from the registry.
	 */
	private void processExtension() throws CoreException {
		String style = configElement.getAttribute(ATT_BORDER_STYLE);
		if (style != null){
			if (style.equalsIgnoreCase("none"))//$NON-NLS-1$
				borderStyle = SWT.NONE;
			else if (style.equalsIgnoreCase("line"))//$NON-NLS-1$
				borderStyle = SWT.BORDER|SWT.FLAT;
		}
		IConfigurationElement [] titleChildren = configElement.getChildren(TAG_TITLE_INFO);
		if (titleChildren.length > 0)
			processTitleInfo(titleChildren[0]);
	}
	
	private void processTitleInfo(IConfigurationElement element) {
		/* get the foreground colors */
		normalTextColor = element.getAttribute(ATT_TITLE_NORMAL_TEXT_COLOR);
		activeTextColor = element.getAttribute(ATT_TITLE_ACTIVE_TEXT_COLOR);
		deactivatedTextColor = element.getAttribute(ATT_TITLE_DEACTIVATED_TEXT_COLOR);
		
		/* get the font */
		titleFont = element.getAttribute(ATT_TITLE_FONT);
		
		/* get the gradient fill direction */
		String direction = element.getAttribute(ATT_TITLE_GRADIENT_DIRECTION);
		if (direction.equalsIgnoreCase("vertical"))//$NON-NLS-1$
			gradientDirection = SWT.VERTICAL;
		
		/* get the color strings */
		normalColors = processColors(element.getAttribute(ATT_TITLE_NORMAL_BG_COLOR));
		activeColors = processColors(element.getAttribute(ATT_TITLE_ACTIVE_BG_COLOR));
		deactivatedColors = processColors(element.getAttribute(ATT_TITLE_DEACTIVATED_BG_COLOR));
		
		/* get the percents */
		normalGradPercents = processPercents( element.getAttribute(ATT_TITLE_NORMAL_BG_PERCENTS));
		activeGradPercents = processPercents(element.getAttribute(ATT_TITLE_ACTIVE_BG_PERCENTS));
		deactivatedGradPercents = processPercents(element.getAttribute(ATT_TITLE_DEACTIVATED_BG_PERCENTS));			
	}
	
	/*
	 * Builds an array out of strings like "50;100;50"
	 */
	private int [] processPercents (String value) {
		if (value != null) {
			value = StringConverter.removeWhiteSpaces(value);
			StringTokenizer stok = new StringTokenizer(value, "|"); //$NON-NLS-1$
			int [] result = new int[stok.countTokens()];
			for (int i = 0; i <=  result.length -1; i++) {
				result[i] = Integer.valueOf(stok.nextToken()).intValue();
			}
			return result;
		}
		return null;
	}
	
	/*
	 * Builds an array out of strings like "255,255,255;123,125,244;55,191,212"
	 */
	private String [] processColors (String value) {
		if (value != null) {
			value = StringConverter.removeWhiteSpaces(value);
			StringTokenizer stok = new StringTokenizer(value, "|"); //$NON-NLS-1$
			String [] result = new String[stok.countTokens()];
			for (int i = 0; i <=  result.length -1; i++) {
				result[i] = stok.nextToken();
			}
			return result;
		}
		return null;
	}
	
	private Color [] createColors (String[] value) {
		Color [] result = new Color [value.length];
		for (int i = 0; i <= result.length-1; i++) {
			result[i] = new Color(null, StringConverter.asRGB(value[i]));
		}
		return result;
	}

}
