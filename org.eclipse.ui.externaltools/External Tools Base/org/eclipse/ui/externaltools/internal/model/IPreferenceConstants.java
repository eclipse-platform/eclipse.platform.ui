package org.eclipse.ui.externaltools.internal.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

/**
 * Constants used to identify user preferences.
 */
public interface IPreferenceConstants {
	
	public static final String PROMPT_FOR_MIGRATION = "externaltools.builders.promptForMigration"; //$NON-NLS-1$
	public static final String SHOW_CONSOLE_FOR_BUILDERS = "externaltools.builders.showConsole"; //$NON-NLS-1$
	
	public static final String CONSOLE_ERROR_RGB = "externaltools.console.errorColor"; //$NON-NLS-1$
	public static final String CONSOLE_WARNING_RGB = "externaltools.console.warningColor"; //$NON-NLS-1$
 	public static final String CONSOLE_INFO_RGB = "externaltools.console.infoColor"; //$NON-NLS-1$
 	public static final String CONSOLE_VERBOSE_RGB = "externaltools.console.verboseColor"; //$NON-NLS-1$
 	public static final String CONSOLE_DEBUG_RGB = "externaltools.console.debugColor"; //$NON-NLS-1$
 	
	public static final String ANTVIEW_INCLUDE_ERROR_SEARCH_RESULTS = "externaltools.antview.includeErrorSearchResults"; //$NON-NLS-1$
	public static final String ANTVIEW_LAST_SEARCH_STRING = "externaltools.antview.lastSearchString"; //$NON-NLS-1$
	public static final String ANTVIEW_LAST_WORKINGSET_SEARCH_SCOPE = "externaltools.antview.lastSearchScope"; //$NON-NLS-1$
	public static final String ANTVIEW_USE_WORKINGSET_SEARCH_SCOPE = "externaltools.antview.useWorkingSetSearchScope"; //$NON-NLS-1$
	
	public static final String ANT_FIND_BUILD_FILE_NAMES = "ant.findBuildFileNames"; //$NON-NLS-1$
}
