package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Constants defining the keys to be used for accessing preferences
 * inside the workbench's preference bundle that are visible to users
 * of the Eclipse UI Framework.
 *
 * In descriptions (of the keys) below describe the preference 
 * stored at the given key. The type indicates type of the stored preferences
 *
 * The preference store is loaded by the plugin (WorkbenchPlugin).
 * @See WorkbenchPlugin.initializeDefaultPreferences() - for initialization of the store
 */
public interface IWorkbenchPreferenceConstants {
	
	// (boolean) Whether or not to select an editor when its associated
	// resource is selected in the navigator (and vice versa)
	public static final String LINK_NAVIGATOR_TO_EDITOR =
		"LINK_NAVIGATOR_TO_EDITOR";

	/**Do we open new in same window, different window or replace the existing one
	 * for shift alt and regular selection
	 */
	public static final String OPEN_NEW_PERSPECTIVE = "OPEN_NEW_PERSPECTIVE";
	public static final String ALTERNATE_OPEN_NEW_PERSPECTIVE =
		"ALTERNATE_OPEN_NEW_PERSPECTIVE";
	public static final String SHIFT_OPEN_NEW_PERSPECTIVE =
		"SHIFT_OPEN_NEW_PERSPECTIVE";

		//Do we swith perspectives for new project and if so which one
	public static final String PROJECT_OPEN_NEW_PERSPECTIVE =
		"PROJECT_OPEN_NEW_PERSPECTIVE";

	//The values possible
	public static final String OPEN_PERSPECTIVE_WINDOW = "OPEN_PERSPECTIVE_WINDOW";
	public static final String OPEN_PERSPECTIVE_PAGE = "OPEN_PERSPECTIVE_PAGE";
	public static final String OPEN_PERSPECTIVE_REPLACE =
		"OPEN_PERSPECTIVE_REPLACE";
	public static final String NO_NEW_PERSPECTIVE = "NO_NEW_PERSPECTIVE";

}
