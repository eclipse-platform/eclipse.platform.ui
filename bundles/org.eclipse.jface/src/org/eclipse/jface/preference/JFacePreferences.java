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
	
	//Identifier for the Color Scheme Inactive Tab Foreground Colour
	public static final String SCHEME_INACTIVE_TAB_FOREGROUND = "SCHEME_INACTIVE_TAB_FOREGROUND";
	//Identifier for the Color Scheme Inactive Tab Background Colour
	public static final String SCHEME_INACTIVE_TAB_BACKGROUND = "SCHEME_INACTIVE_TAB_BACKGROUND";
	//Identifier for the Color Scheme Background Colour
	public static final String SCHEME_PARENT_BACKGROUND_COLOR = "SCHEME_PARENT_BACKGROUND_COLOR"; //$NON-NLS-1$
	public static final String SCHEME_BACKGROUND_COLOR = "SCHEME_BACKGROUND_COLOR"; //$NON-NLS-1$
	public static final String SCHEME_BORDER_COLOR = "SCHEME_BORDER_COLOR"; //$NON-NLS-1$
	//Identifier for the Color Scheme Foreground Colour
	public static final String SCHEME_FOREGROUND_COLOR = "SCHEME_FOREGROUND_COLOR"; //$NON-NLS-1$
	//Identifier for the Color Scheme Selection Background Colour
	public static final String SCHEME_SELECTION_BACKGROUND_COLOR = "SCHEME_SELECTION_BACKGROUND_COLOR"; //$NON-NLS-1$
	//Identifier for the Color Scheme Selection Foreground Colour
	public static final String SCHEME_SELECTION_FOREGROUND_COLOR = "SCHEME_SELECTION_FOREGROUND_COLOR"; //$NON-NLS-1$
	
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
