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
package org.eclipse.ui.externaltools.internal.model;


/**
 * Constants used to identify user preferences.
 */
public interface IPreferenceConstants {
	
	public static final String PROMPT_FOR_MIGRATION = "externaltools.builders.promptForMigration"; //$NON-NLS-1$
	
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
