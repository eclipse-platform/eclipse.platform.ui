package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * Constants defining the keys to be used for accessing preferences
 * inside the workbench's preference bundle.
 *
 * In descriptions (of the keys) below describe the preference 
 * stored at the given key. The type indicates type of the stored preferences
 *
 * The preference store is loaded by the plugin (WorkbenchPlugin).
 * @See WorkbenchPlugin.initializeDefaultPreferences() - for initialization of the store
 */
public interface IWorkbenchPreferenceConstants {
	// (boolean) Whether or not to display the Welcome dialog on startup.
	public static final String WELCOME_DIALOG = "WELCOME_DIALOG";

	// (int) Current presentation style the workbench is using
	public static final String VIEW_PRESENTATION_STYLE = "VIEW_PRESENTATION_STYLE";

	// (boolean) Whether or not to select an editor when its associated
	// resource is selected in the navigator (and vice versa)
	public static final String LINK_NAVIGATOR_TO_EDITOR =
		"LINK_NAVIGATOR_TO_EDITOR";

	// (boolean) Cause all editors to save modified resources prior
	// to running a full or incremental manual build.
	public static final String SAVE_ALL_BEFORE_BUILD = "SAVE_ALL_BEFORE_BUILD";

	// (boolean) Whether or not to automatically run a build
	// when a resource is modified. NOTE: The value is not
	// actually in the preference store but available from
	// IWorkspace. This constant is used solely for property
	// change notification from the preference store so
	// interested parties can listen for all preference changes.
	public static final String AUTO_BUILD = "AUTO_BUILD";

	//Do we show tabs up or down for views
	public static final String VIEW_TAB_POSITION = "VIEW_TAB_POSITION";

	//Do we show tabs up or down for editors
	public static final String EDITOR_TAB_POSITION = "EDITOR_TAB_POSITION";

	/**Do we open new in same window, different window or replace the existing one
	 * for shift alt and regular selection
	 */
	public static final String OPEN_NEW_PERSPECTIVE = "OPEN_NEW_PERSPECTIVE";
	public static final String ALT_OPEN_NEW_PERSPECTIVE =
		"ALT_OPEN_NEW_PERSPECTIVE";
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
