package org.eclipse.debug.internal.ui.preferences;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Constants defining the keys to be used for accessing preferences
 * inside the debug ui plugin's preference bundle.
 *
 * In descriptions (of the keys) below describe the preference 
 * stored at the given key. The type indicates type of the stored preferences
 *
 * The preference store is loaded by the plugin (DebugUIPlugin).
 * @see DebugUIPlugin.initializeDefaultPreferences(IPreferenceStore) - for initialization of the store
 */
public interface IDebugPreferenceConstants {

	/**
	 * RGB colors for displaying the content in the Console
	 */
	public static final String CONSOLE_SYS_ERR_RGB= "Console.stdErrColor"; //$NON-NLS-1$
	public static final String CONSOLE_SYS_OUT_RGB= "Console.stdOutColor"; //$NON-NLS-1$
	public static final String CONSOLE_SYS_IN_RGB= "Console.stdInColor"; //$NON-NLS-1$
			
	/**
	 * The name of the font to use for the Console
	 **/
	public static final String CONSOLE_FONT= "Console.font"; //$NON-NLS-1$
	
	/**
	 * (boolean) Whether or not the console view is shown 
	 * when there is program output.
  	 */
	public static final String CONSOLE_OPEN_ON_OUT= "DEBUG.consoleOpenOnOut"; //$NON-NLS-1$
	/**
	 * (boolean) Whether or not the console view is shown 
	 * when there is program error.
  	 */
	public static final String CONSOLE_OPEN_ON_ERR= "DEBUG.consoleOpenOnErr"; //$NON-NLS-1$
	
	/**
	 * The orientation of the detail view in the VariablesView
	 */
	public static final String VARIABLES_DETAIL_PANE_ORIENTATION = "Variables.detail.orientation"; //$NON-NLS-1$
	public static final String VARIABLES_DETAIL_PANE_RIGHT = "Variables.detail.orientation.right"; //$NON-NLS-1$
	public static final String VARIABLES_DETAIL_PANE_UNDERNEATH = "Variables.detail.orientation.underneath"; //$NON-NLS-1$
	
	/**
	 * The RGB for the color to be used to indicate changed variables
	 */
	public static final String CHANGED_VARIABLE_RGB= "Changed.Variable.RGB"; //$NON-NLS-1$
	
	/**
	 * Memento for the last selected launch config in the
	 * launch config dialog.
	 */
	public static final String PREF_LAST_LAUNCH_CONFIGURATION_SELECTION = IDebugUIConstants.PLUGIN_ID + ".lastLaunchConfigSelection"; //$NON-NLS-1$

	/**
	 * Serialized list of the enabled debug action groups
	 */	
	public static final String PREF_ENABLED_DEBUG_ACTION_GROUPS = IDebugUIConstants.PLUGIN_ID + "enabledDebugActionGroups"; //$NON-NLS-1$
	
	/**
	 * Serialized list of the disabled debug action groups
	 */	
	public static final String PREF_DISABLED_DEBUG_ACTION_GROUPS = IDebugUIConstants.PLUGIN_ID + "disabledDebugActionGroups"; //$NON-NLS-1$
	
	/**
	 * The maximum size of the launch history list
	 */
	public static int MAX_LAUNCH_HISTORY_SIZE= 15;
}


