/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets;

public interface ICheatSheetResource {


	//
	// ID used through out the cheatsheets:
	//		plugin, menu, view
	//
	public static final String CHEAT_SHEET_PLUGIN_ID = "org.eclipse.ui.cheatsheets"; //$NON-NLS-1$
	public static final String CHEAT_SHEET_MENU_ID = "org.eclipse.ui.cheatsheets.menu"; //$NON-NLS-1$
	public static final String CHEAT_SHEET_VIEW_ID = "org.eclipse.ui.cheatsheets.views.CheatSheetView"; //$NON-NLS-1$
	public static final String CHEAT_SHEET_RESOURCE_ID = "org.eclipse.ui.internal.cheatsheets.CheatsheetPluginResources"; //$NON-NLS-1$

	// cheatsheet state data file
	public static final String CHEAT_SHEET_SAVE_FILE="cheatsheet.dat"; //$NON-NLS-1$

	// cheatsheet help page
	public static final String CHEAT_SHEET_HELP_PAGE="/org.eclipse.ui.cheatsheets.doc/tasks/tcheatst.htm";//$NON-NLS-1$

	// Memento info
	public static final String MEMENTO = "cheatSheetMemento"; //$NON-NLS-1$
	public static final String MEMENTO_ID = "id"; //$NON-NLS-1$
	public static final String MEMENTO_NAME = "name"; //$NON-NLS-1$
	public static final String MEMENTO_URL = "url"; //$NON-NLS-1$




	//
	// Constants used to retrieve strings from the cheatsheet resource bundle.
	//
	public static final String CHEAT_SHEET_SELECTION_DIALOG_TITLE = "%CHEAT_SHEET_SELECTION_DIALOG_TITLE"; //$NON-NLS-1$
	public static final String CHEAT_SHEET_SELECTION_DIALOG_MSG = "%CHEAT_SHEET_SELECTION_DIALOG_MSG"; //$NON-NLS-1$

	public static final String COLLAPSE_ALL_BUT_CURRENT_TOOLTIP = "%COLLAPSE_ALL_BUT_CURRENT_TOOLTIP"; //$NON-NLS-1$
	public static final String RESTORE_ALL_TOOLTIP = "%RESTORE_ALL_TOOLTIP"; //$NON-NLS-1$

	public static final String CHEAT_SHEET_OTHER_MENU = "%CHEAT_SHEET_OTHER_MENU"; //$NON-NLS-1$

	public static final String CHEAT_SHEETS = "%CHEAT_SHEETS"; //$NON-NLS-1$

	public static final String PERFORM_TASK_TOOLTIP = "%PERFORM_TASK_TOOLTIP"; //$NON-NLS-1$
	public static final String SKIP_TASK_TOOLTIP = "%SKIP_TASK_TOOLTIP"; //$NON-NLS-1$
	public static final String COMPLETE_TASK_TOOLTIP = "%COMPLETE_TASK_TOOLTIP"; //$NON-NLS-1$
	public static final String START_CHEATSHEET_TOOLTIP = "%START_CHEATSHEET_TOOLTIP"; //$NON-NLS-1$
	public static final String RESTART_CHEATSHEET_TOOLTIP = "%RESTART_CHEATSHEET_TOOLTIP"; //$NON-NLS-1$
	public static final String RESTART_TASK_TOOLTIP = "%RESTART_TASK_TOOLTIP"; //$NON-NLS-1$

	public static final String VIEW_READFILE_ACCESSEXCEPTION = "%VIEW_READFILE_ACCESSEXCEPTION"; //$NON-NLS-1$

	public static final String ERROR_OPENING_FILE_TITLE = "%ERROR_OPENING_FILE_TITLE"; //$NON-NLS-1$
	public static final String ERROR_OPENING_FILE = "%ERROR_OPENING_FILE"; //$NON-NLS-1$
	public static final String ERROR_PARSING_ITEMS = "%ERROR_PARSING_ITEMS"; //$NON-NLS-1$
	public static final String ERROR_OPENING_FILE_IN_PARSER = "%ERROR_OPENING_FILE_IN_PARSER"; //$NON-NLS-1$
	public static final String ERROR_SAX_PARSING = "%ERROR_SAX_PARSING"; //$NON-NLS-1$
	public static final String CHEAT_SHEET_INTRO_TITLE = "%CHEAT_SHEET_INTRO_TITLE"; //$NON-NLS-1$

	public static final String ERROR_READING_STATE_FILE = "ERROR_READING_STATE_FILE"; //$NON-NLS-1$
	public static final String ERROR_WRITING_STATE_FILE = "ERROR_WRITING_STATE_FILE"; //$NON-NLS-1$

	public static final String ERROR_RUNNING_ACTION = "%ERROR_RUNNING_ACTION"; //$NON-NLS-1$
	public static final String ERROR_FINDING_PLUGIN_FOR_ACTION = "%ERROR_FINDING_PLUGIN_FOR_ACTION"; //$NON-NLS-1$
	public static final String ERROR_LOADING_CLASS_FOR_ACTION = "%ERROR_LOADING_CLASS_FOR_ACTION"; //$NON-NLS-1$
	public static final String ERROR_CREATING_CLASS_FOR_ACTION = "%ERROR_CREATING_CLASS_FOR_ACTION"; //$NON-NLS-1$
	public static final String ERROR_LOADING_CHEATSHEET_CONTENT = "%ERROR_LOADING_CHEATSHEET_CONTENT"; //$NON-NLS-1$
	public static final String ERROR_PAGE_MESSAGE ="%ERROR_PAGE_MESSAGE"; //$NON-NLS-1$

	public static final String LAUNCH_SHEET_ERROR = "%LAUNCH_SHEET_ERROR"; //$NON-NLS-1$
	public static final String CHEAT_SHEET_ERROR_OPENING = "%CHEAT_SHEET_ERROR_OPENING"; //$NON-NLS-1$

	public static final String CHEAT_SHEET_OTHER_CATEGORY = "%CHEAT_SHEET_OTHER_CATEGORY"; //$NON-NLS-1$
	
	public static final String HELP_BUTTON_TOOLTIP = "%HELP_BUTTON_TOOLTIP";//$NON-NLS-1$
	public static final String LESS_THAN_2_SUBITEMS = "%LESS_THAN_2_SUBITEMS";//$NON-NLS-1$
	
}

