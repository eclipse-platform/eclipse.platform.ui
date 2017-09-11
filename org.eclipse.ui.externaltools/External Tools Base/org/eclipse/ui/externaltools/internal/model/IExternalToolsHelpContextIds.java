/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.model;


/**
 * Help context ids for the external tools.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 */
public interface IExternalToolsHelpContextIds {
	public static final String PREFIX = "org.eclipse.ui.externaltools."; //$NON-NLS-1$
	
	// Preference Pages
	public static final String EXTERNAL_TOOLS_PREFERENCE_PAGE = PREFIX + "preference_page_context";  //$NON-NLS-1$
		
	// Property Pages
	public static final String EXTERNAL_TOOLS_BUILDER_PROPERTY_PAGE = PREFIX + "builder_property_page_context"; //$NON-NLS-1$
	
	//Dialogs
	public static final String MESSAGE_WITH_TOGGLE_DIALOG = PREFIX + "message_with_toggle_dialog_context"; //$NON-NLS-1$
	public static final String FILE_SELECTION_DIALOG = PREFIX + "file_selection_dialog_context"; //$NON-NLS-1$
	
	//Launch configuration dialog tabs
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_BUILDER_TAB = PREFIX + "builders_tab_context"; //$NON-NLS-1$
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_PROGRAM_MAIN_TAB = PREFIX + "program_main_tab_context"; //$NON-NLS-1$
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_BUILD_TAB = PREFIX + "build_tab_context"; //$NON-NLS-1$
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_REFRESH_TAB = PREFIX + "refresh_tab_context"; //$NON-NLS-1$
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_ENVIRONMENT_TAB = PREFIX + "environment_tab_context"; //$NON-NLS-1$
	public static final String EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_COMMON_TAB = PREFIX + "common_tab_context"; //$NON-NLS-1$
}
