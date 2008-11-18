/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.console;

/**
 * Constants relating to the console plug-in.
 * 
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IConsoleConstants {
	
	/**
	 * Console plug-in identifier (value <code>"org.eclipse.ui.console"</code>).
	 */
	public static final String PLUGIN_ID = ConsolePlugin.getUniqueIdentifier();
	
	/**
	 * Console view identifier (value <code>"org.eclipse.ui.console.ConsoleView"</code>).
	 */
	public static final String ID_CONSOLE_VIEW= "org.eclipse.ui.console.ConsoleView"; //$NON-NLS-1$

	/**
	 * Type identifier for MessageConsole
	 * @since 3.1
	 */
    public static final String MESSAGE_CONSOLE_TYPE = "org.eclipse.ui.MessageConsole"; //$NON-NLS-1$
	
	/**
	 * The name of the font to use for the Console (value <code>"org.eclipse.ui.console.ConsoleFont"</code>).
	 * 
	 * @deprecated When a console is created, its font is set to the default text font as specified
	 * by <code>JFaceResources.TEXT_FONT</code>. Clients must provide their own infrastructure to
	 * manage console specific fonts. 
	 */ 
	public static final String CONSOLE_FONT= "org.eclipse.ui.console.ConsoleFont"; //$NON-NLS-1$
	
	/**
	 * Menu group identifier for the console view context menu and toolbar, for actions pertaining to
	 * launching (value <code>"launchGroup"</code>).
	 */
	public static final String LAUNCH_GROUP = "launchGroup"; //$NON-NLS-1$

	/**
	 * Menu group identifier for the console view context menu and toolbar, for actions pertaining to
	 * console output. (value<code>"outputGroup"</code>).
	 */
	public static final String OUTPUT_GROUP = "outputGroup"; //$NON-NLS-1$	
		
	/** 
	 * Console view image identifier.
	 */
	public static final String IMG_VIEW_CONSOLE= "IMG_VIEW_CONSOLE"; //$NON-NLS-1$
	
	/** 
	 * Clear action image identifier. 
	 */
	public static final String IMG_LCL_CLEAR= "IMG_LCL_CLEAR"; //$NON-NLS-1$
		
	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int INTERNAL_ERROR = 120;

	/**
	 * Console pattern match listeners extension point identifier
	 * (value <code>"consolePatternMatchListeners"</code>).
	 * 
	 * @since 3.1 
	 */
    public static final String EXTENSION_POINT_CONSOLE_PATTERN_MATCH_LISTENERS = "consolePatternMatchListeners"; //$NON-NLS-1$

    /**
     * Console page participants extension point identifier
     * (value <code>"consolePageParticipants"</code>).
     * 
     * @since 3.1
     */
    public static final String EXTENSION_POINT_CONSOLE_PAGE_PARTICIPANTS = "consolePageParticipants"; //$NON-NLS-1$

    /**
     * Console factories extension point identifier
     * (value <code>"consoleFactories"</code>).
     * 
     * @since 3.1
     */
    public static final String EXTENSION_POINT_CONSOLE_FACTORIES = "consoleFactories"; //$NON-NLS-1$
    
	/**
	 * Property constant indicating a console's font has changed.
	 *  
	 * @since 3.1
	 */
	public static final String P_FONT = ConsolePlugin.getUniqueIdentifier() + ".P_FONT"; //$NON-NLS-1$
	
	/**
	 * Property constant indicating that a font style has changed
	 * 
	 * @since 3.1
	 */
	public static final String P_FONT_STYLE = ConsolePlugin.getUniqueIdentifier() + ".P_FONT_STYLE"; //$NON-NLS-1$
	
	/**
	 * Property constant indicating the color of a stream has changed.
	 * 
	 *  @since 3.1
	 */
	public static final String P_STREAM_COLOR = ConsolePlugin.getUniqueIdentifier()  + ".P_STREAM_COLOR";	 //$NON-NLS-1$
		
	/**
	 * Property constant indicating tab size has changed
	 * 
	 *  @since 3.1
	 */
	public static final String P_TAB_SIZE = ConsolePlugin.getUniqueIdentifier()  + ".P_TAB_SIZE";	 //$NON-NLS-1$
	
	/**
	 * Property constant indicating the width of a fixed width console has changed.
	 * 
	 * @since 3.1
	 */
	public static final String P_CONSOLE_WIDTH = ConsolePlugin.getUniqueIdentifier() + ".P_CONSOLE_WIDTH"; //$NON-NLS-1$
	
	/**
	 * Property constant indicating that all streams connected to this console have been closed
	 * and that all queued output has been processed.
	 * 
	 * @since 3.1
	 */
	public static final String P_CONSOLE_OUTPUT_COMPLETE = ConsolePlugin.getUniqueIdentifier() + ".P_CONSOLE_STREAMS_CLOSED"; //$NON-NLS-1$
	
	/**
	 * Property constant indicating the background color of a console has changed.
	 * 
	 *  @since 3.3
	 */
	public static final String P_BACKGROUND_COLOR = ConsolePlugin.getUniqueIdentifier()  + ".P_BACKGROUND_COLOR";	 //$NON-NLS-1$	

    /**
     * The default tab size for text consoles.
     * 
     * @since 3.1
     */
    public static final int DEFAULT_TAB_SIZE = 8;

}
