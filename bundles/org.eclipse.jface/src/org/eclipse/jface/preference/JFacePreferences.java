/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.preference;

import org.eclipse.jface.resource.JFaceColors;

/**
 * 
 * JFacePreferences is a class used to administer the preferences
 * used by JFace objects.
 */
public final class JFacePreferences {
	
	//Identifier for the Error Colour
	public static final String ERROR_COLOR = "ERROR_COLOR"; //$NON-NLS-1$
	//Identifier for the Hyperlink Colour
	public static final String HYPERLINK_COLOR = "HYPERLINK_COLOR"; //$NON-NLS-1$
	//Identifier for the Active Hyperlink Colour
	public static final String ACTIVE_HYPERLINK_COLOR = "ACTIVE_HYPERLINK_COLOR"; //$NON-NLS-1$
	//Do we use the System theme?
	public static final String USE_DEFAULT_THEME = "USE_DEFAULT_THEME"; //$NON-NLS-1$
	
	//Identifier for the Color Scheme Inactive Selected Tab Background Colour
	public static final String SCHEME_TAB_INACTIVE_SELECTION_BACKGROUND = JFaceColors.SCHEME_TAB_INACTIVE_SELECTION_BACKGROUND;

	//Identifier for the Color Scheme Selected Tab Foreground Colour
	public static final String SCHEME_TAB_SELECTION_FOREGROUND = JFaceColors.SCHEME_TAB_SELECTION_FOREGROUND;
	//Identifier for the Color Scheme Selected Tab Background Colour
	public static final String SCHEME_TAB_SELECTION_BACKGROUND = JFaceColors.SCHEME_TAB_SELECTION_BACKGROUND;
	//Identifier for the Color Scheme Tab Foreground Colour
	public static final String SCHEME_TAB_FOREGROUND = JFaceColors.SCHEME_TAB_FOREGROUND;
	//Identifier for the Color Scheme Tab Background Colour
	public static final String SCHEME_TAB_BACKGROUND = JFaceColors.SCHEME_TAB_BACKGROUND;
	
	public static final String SCHEME_BACKGROUND_COLOR = JFaceColors.SCHEME_BACKGROUND; //$NON-NLS-1$
	//Identifier for the Color Scheme Foreground Colour
	public static final String SCHEME_FOREGROUND_COLOR = JFaceColors.SCHEME_FOREGROUND;
	
	private static IPreferenceStore preferenceStore;
	/**
	 * Prevent construction.
	 */
	private JFacePreferences() {
	}
	
	/**
	 * Return the preference store for the receiver.
	 * @return IPreferenceStore or null
	 */
	public static IPreferenceStore getPreferenceStore(){
		return preferenceStore;
	}
	
	/**
	 * Set the preference store for the receiver.
	 * @param store IPreferenceStore
	 */
	public static void setPreferenceStore(IPreferenceStore store){
		preferenceStore = store;
	}

}
