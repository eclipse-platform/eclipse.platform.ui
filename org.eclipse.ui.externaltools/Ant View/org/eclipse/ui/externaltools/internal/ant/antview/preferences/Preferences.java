/*
 * Copyright (c) 2002, Roscoe Rush. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 0.5 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclipse.org/
 *
 */
package org.eclipse.ui.externaltools.internal.ant.antview.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.externaltools.internal.ant.antview.core.IAntViewConstants;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;

public class Preferences {
	/**
	 * Method setDefaults.
	 */
	public static void setDefaults() { 
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(IAntViewConstants.PREF_ANT_DISPLAY,     IAntViewConstants.ANT_DISPLAYLVL_INFO);
		store.setDefault(IAntViewConstants.PREF_PROJECT_DISPLAY, IAntViewConstants.PROJECT_DISPLAY_DIRLOC);
		store.setDefault(IAntViewConstants.PREF_TARGET_DISPLAY,  IAntViewConstants.TARGET_DISPLAY_NAMEATTR);
		store.setDefault(IAntViewConstants.PREF_TARGET_FILTER,   IAntViewConstants.TARGET_FILTER_NONE);
		store.setDefault(IAntViewConstants.PREF_ANT_BUILD_FILE,  org.apache.tools.ant.Main.DEFAULT_BUILD_FILENAME);
	}
	/**
	 * Method getString.
	 * @param key
	 * @return String
	 */
	public static String getString(String key) { 
		IPreferenceStore store = getPreferenceStore();
		return store.getString(key);	
	}	
	/**
	 * Method setString.
	 * @param key
	 * @param value
	 */
	public static void setString(String key, String value) {	
		IPreferenceStore store = getPreferenceStore();
		store.setValue(key,value);
	}
	/**
	 * Method getPreferenceStore.
	 * @return IPreferenceStore
	 */
	public static IPreferenceStore getPreferenceStore() { 
		return ExternalToolsPlugin.getDefault().getPreferenceStore();
	}
}
