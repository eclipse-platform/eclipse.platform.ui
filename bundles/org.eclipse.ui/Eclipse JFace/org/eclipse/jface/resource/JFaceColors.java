package org.eclipse.jface.resource;

/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
 
import java.util.*;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * JFaceColors is the class that stores references
 * to all of the colors used by JFace.
 */

public class JFaceColors {
	
	private static Hashtable colorTable = new Hashtable();
	//Keep a list of the Colors we have allocated seperately
	//as system colors do not need to be disposed.
	private static ArrayList allocatedColors = new ArrayList();

	/**
	 * Get the Color used for banner backgrounds
	 */

	public static Color getBannerBackground(Display display) {
		return display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	}
	
	/**
	 * Get the Color used for banner foregrounds
	 */

	public static Color getBannerForeground(Display display) {
		return display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
	}

	/**
	 * Get the background Color for widgets that
	 * display errors.
	 */

	public static Color getErrorBackground(Display display) {
		return display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	}

	/**
	 * Get the border Color for widgets that
	 * display errors.
	 */

	public static Color getErrorBorder(Display display) {
		return display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
	}

	/**
	 * Get the defualt color to use for displaying errors.
	 */
	public static Color getErrorText(Display display) {

		return getColorSetting(display,JFacePreferences.ERROR_COLOR);
	}
	
	/**
	 * Get the default color to use for displaying hyperlinks.
	 */
	public static Color getHyperlinkText(Display display) {

		return getColorSetting(display,JFacePreferences.HYPERLINK_COLOR);
	}
	
	/**
	 * Get the default color to use for displaying active hyperlinks.
	 */
	public static Color getActiveHyperlinkText(Display display) {

		return getColorSetting(display,JFacePreferences.ACTIVE_HYPERLINK_COLOR);
	}
	
	/**
	 * Clear out the cached color for name. This is generally
	 * done when the color preferences changed and any cached colors
	 * may be disposed. Users of the colors in this class should add a IPropertyChangeListener
	 * to detect when any of these colors change.
	 */
	public static void clearColor(String colorName){
		colorTable.remove(colorName);
		//We do not dispose here for backwards compatibility
	}
	
	/**
	 * Get the color setting for the name.
	 */
	private static Color getColorSetting(Display display, String preferenceName) {

		if(colorTable.contains(preferenceName))
			return (Color) colorTable.get(preferenceName);
			
		IPreferenceStore store = JFacePreferences.getPreferenceStore();
		if (store == null){
			//Dark blue is the default if there is no store
			Color color = getDefaultColor(display,preferenceName);
			colorTable.put(preferenceName,color);
			return color;
		}
		else{
			Color color = new Color(
				display,
				PreferenceConverter.getColor(store, preferenceName));
			allocatedColors.add(color);
			colorTable.put(preferenceName,color);
			return color;
		}
	}
	
	/**
	 * Return the default color for the preferenceName. If there is
	 * no setting return the system black.
	 */
	private static Color getDefaultColor(Display display,String preferenceName){
		
		if(preferenceName.equals(JFacePreferences.ERROR_COLOR))
			return display.getSystemColor(SWT.COLOR_RED);
		if(preferenceName.equals(JFacePreferences.HYPERLINK_COLOR)){
			Color color = new Color(display,0,0,153);
			allocatedColors.add(color);
			return color;
		}
		if(preferenceName.equals(JFacePreferences.ACTIVE_HYPERLINK_COLOR))
			return display.getSystemColor(SWT.COLOR_BLUE);
		return display.getSystemColor(SWT.COLOR_BLACK);
	}
	
	/**
	 * Dispose of all allocated colors. Called on workbench
	 * shutdown.
	 */
	public static void disposeColors(){
		Iterator colors = allocatedColors.iterator();
		while(colors.hasNext()){
			((Color) colors.next()).dispose();
		}
	}
	
	/**
	 * Set the foreground and background colors of the
	 * control to the specified values. If the values are
	 * null than ignore them.
	 * @param foreground Color
	 * @param background Color
	 */
	public static void setColors(Control control,Color foreground, Color background){
		if(foreground != null)
			control.setForeground(foreground);
		if(background != null)
			control.setBackground(background);
	}
		
}