/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.Gradient;
import org.eclipse.jface.resource.GradientRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.themes.ITheme;

/**
 * ColorSchemeService is the service that sets the colors on widgets as
 * appropriate.
 * 
 * TODO: do we need this?  can this be expanded or removed entirely?
 */
public class ColorSchemeService {

	public static void setTabColors(ITheme theme, CTabFolder control) {	  
	    ColorRegistry colorRegistry = theme.getColorRegistry();
	    GradientRegistry gradientRegistry = theme.getGradientRegistry();

	    Color fgColor;
	    Gradient bgGradient;

		fgColor = colorRegistry.get(IWorkbenchPresentationConstants.ACTIVE_TAB_TEXT_COLOR);
		bgGradient = gradientRegistry.get(IWorkbenchPresentationConstants.TAB_BG_GRADIENT);
	    
		control.setBackground(bgGradient.getColors(), bgGradient.getPercents(), bgGradient.getDirection() == SWT.VERTICAL); 
		control.setForeground(fgColor);		
		
		fgColor = colorRegistry.get(IWorkbenchPresentationConstants.ACTIVE_TAB_TEXT_COLOR);
		bgGradient = gradientRegistry.get(IWorkbenchPresentationConstants.ACTIVE_TAB_BG_GRADIENT);
		
		control.setSelectionBackground(bgGradient.getColors(), bgGradient.getPercents(), bgGradient.getDirection() == SWT.VERTICAL); 
		control.setSelectionForeground(fgColor);		
			
	}

	/**
	 * @param control
	 */
	public static void setCoolBarColors(ITheme theme, Control control) {
		setBasicColors(theme, control);
	}

	private static void setBasicColors(ITheme theme, Control control) {	    
		control.setBackground(
		        theme.getColorRegistry().get(IWorkbenchPresentationConstants.BACKGROUND));
		control.setForeground(
		        theme.getColorRegistry().get(IWorkbenchPresentationConstants.FOREGROUND));
	}

	/**
	 * @param bar
	 */
	public static void setPerspectiveToolBarColors(ITheme theme, ToolBar control) {
		setBasicColors(theme, control);
	}
}
