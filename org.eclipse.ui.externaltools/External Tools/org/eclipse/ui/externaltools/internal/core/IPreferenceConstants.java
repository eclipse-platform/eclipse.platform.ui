package org.eclipse.ui.externaltools.internal.core;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/

/**
 * Constants used to identify user preferences.
 */
public interface IPreferenceConstants {
	public static final String AUTO_SAVE = "externaltools.auto_save";

//	public static final String OUPUT_LEVEL = "externaltools.outputLevel";
 	public static final String INFO_LEVEL = "externaltools.infoLevel";
 	public static final String VERBOSE_LEVEL = "externaltools.verboseLevel";
 	public static final String DEBUG_LEVEL = "externaltools.levelLevel";
	
	public static final String CONSOLE_ERROR_RGB = "externaltools.console.errorColor";
	public static final String CONSOLE_WARNING_RGB = "externaltools.console.warningColor";
 	public static final String CONSOLE_INFO_RGB = "externaltools.console.infoColor";
 	public static final String CONSOLE_VERBOSE_RGB = "externaltools.console.verboseColor";
 	public static final String CONSOLE_DEBUG_RGB = "externaltools.console.debugColor";
 	public static final String CONSOLE_FONT = "externaltools.console.font";
}
