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

package org.eclipse.ui.console;

public interface IConsoleConstants {
	
	/**
	 * Consoleplug-in identifier (value <code>"org.eclipse.ui.console"</code>).
	 */
	public static final String PLUGIN_ID = ConsolePlugin.getUniqueIdentifier();
	
	/**
	 * Console view identifier (value <code>"org.eclipse.ui.console.ConsoleView"</code>).
	 */
	public static final String ID_CONSOLE_VIEW= "org.eclipse.ui.console.ConsoleView"; //$NON-NLS-1$
	
	/**
	 * The name of the font to use for the Console. 
	 */ 
	public static final String CONSOLE_FONT= "org.eclipse.ui.console.ConsoleFont"; //$NON-NLS-1$
	
	/**
	 * Identifier for a launch group in a menu (value <code>"launchGroup"</code>).
	 */
	public static final String LAUNCH_GROUP = "launchGroup"; //$NON-NLS-1$

	/**
	 * Identifier for an output group in a menu (value
	 * <code>"outputGroup"</code>).
	 */
	public static final String OUTPUT_GROUP = "outputGroup"; //$NON-NLS-1$	
		
	/** 
	 * Console view image identifier
	 * 
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
}
