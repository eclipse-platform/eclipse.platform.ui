package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * The IPreferenceConstants are the internal constants used by the Workbench.
 */
public interface IPreferenceConstants {
	// (boolean) Whether or not to display the Welcome dialog on startup.
	public static final String WELCOME_DIALOG = "WELCOME_DIALOG"; //$NON-NLS-1$

	// (boolean) Cause all editors to save modified resources prior
	// to running a full or incremental manual build.
	public static final String SAVE_ALL_BEFORE_BUILD = "SAVE_ALL_BEFORE_BUILD"; //$NON-NLS-1$

	// (boolean) Whether or not to automatically run a build
	// when a resource is modified. NOTE: The value is not
	// actually in the preference store but available from
	// IWorkspace. This constant is used solely for property
	// change notification from the preference store so
	// interested parties can listen for all preference changes.
	public static final String AUTO_BUILD = "AUTO_BUILD"; //$NON-NLS-1$

	//Do we show tabs up or down for views
	public static final String VIEW_TAB_POSITION = "VIEW_TAB_POSITION"; //$NON-NLS-1$

	//Boolean: true = single click opens editor; false = double click opens it.
	public static final String OPEN_ON_SINGLE_CLICK = "OPEN_ON_SINGLE_CLICK"; //$NON-NLS-1$
	//Boolean: true = select on hover;
	public static final String SELECT_ON_HOVER = "SELECT_ON_HOVER"; //$NON-NLS-1$
	//Boolean: true = open after delay
	public static final String OPEN_AFTER_DELAY = "OPEN_AFTER_DELAY"; //$NON-NLS-1$

	//Do we show tabs up or down for editors
	public static final String EDITOR_TAB_POSITION = "EDITOR_TAB_POSITION"; //$NON-NLS-1$

	// (int) If > 0, an editor will be reused once 'N' editors are opened.
	public static final String REUSE_EDITORS = "REUSE_OPEN_EDITORS"; //$NON-NLS-1$
	//On/Off option for the preceding option.
	public static final String REUSE_EDITORS_BOOLEAN = "REUSE_OPEN_EDITORS_BOOLEAN"; //$NON-NLS-1$
	
	// (int) N recently viewed files will be listed in the File->Open Recent menu.
	public static final String RECENT_FILES = "RECENT_FILES"; //$NON-NLS-1$
	public static final int MAX_RECENT_FILES_SIZE = 15;

	// (integer) Mode for opening a view.
	public static final String OPEN_VIEW_MODE = "OPEN_VIEW_MODE"; //$NON-NLS-1$
	public static final int OVM_EMBED = 0;
	public static final int OVM_FAST = 1;
	public static final int OVM_FLOAT = 2;

	//Identifier for enabled decorators
	public static final String ENABLED_DECORATORS = "ENABLED_DECORATORS"; //$NON-NLS-1$
	
	//Boolean: true = other encoding selected; false = default encoding selected
	public static final String IS_ENCODING_SET = "IS_ENCODING_SET"; //$NON-NLS-1$
	//String: the selected encoding, only valid if IS_ENCODING_SET is true
	public static final String ENCODING = "ENCODING"; //$NON-NLS-1$
}