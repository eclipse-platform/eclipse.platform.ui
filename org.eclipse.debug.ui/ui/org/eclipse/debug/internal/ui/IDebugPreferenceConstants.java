package org.eclipse.debug.internal.ui;

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
 * @See DebugUIPlugin.initializeDefaultPreferences() - for initialization of the store
 */
public interface IDebugPreferenceConstants {

	/**
	 * RGB colors for displaying the content in the Console
	 */
	public static final String CONSOLE_SYS_ERR_RGB= "Console.stdErrColor";
	public static final String CONSOLE_SYS_OUT_RGB= "Console.stdOutColor";
	public static final String CONSOLE_SYS_IN_RGB= "Console.stdInColor";
	
	public static final String CONSOLE_MAX_OUTPUT_SIZE= "Console.maxOutputSize";	
			
	/**
	 * The name of the font to use for the Console
	 **/
	public static final String CONSOLE_FONT= "Console.font";
	
	/**
	 * (boolean) Whether or not the console view is shown 
	 * when there is program ouptut.
  	 */
	public static final String CONSOLE_OPEN= "DEBUG.consoleOpen";


}


