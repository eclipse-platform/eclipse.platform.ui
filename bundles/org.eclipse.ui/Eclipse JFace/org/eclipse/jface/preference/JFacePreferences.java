package org.eclipse.jface.preference;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * 
 * JFacePreferences is a class used to administer the preferences
 * used by JFace pbjects.
 */
public final class JFacePreferences {
	
	//Identifier for the Error Colour
	public static final String ERROR_COLOR = "ERROR_COLOR"; //$NON-NLS-1$
	//Identifier for the Hyperlink Colour
	public static final String HYPERLINK_COLOR = "HYPERLINK_COLOR"; //$NON-NLS-1$

	
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
