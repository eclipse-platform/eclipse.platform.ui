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

	// Empty string used through out the plugin
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	//
	// ID used through out the cheatsheets:
	//		plugin, menu, view
	//
	public static final String CHEAT_SHEET_PLUGIN_ID = "org.eclipse.ui.cheatsheets"; //$NON-NLS-1$
	public static final String CHEAT_SHEET_VIEW_ID = "org.eclipse.ui.cheatsheets.views.CheatSheetView"; //$NON-NLS-1$
	public static final String CHEAT_SHEET_RESOURCE_ID = "org.eclipse.ui.internal.cheatsheets.CheatsheetPluginResources"; //$NON-NLS-1$

	// Memento info
	public static final String MEMENTO = "cheatSheetMemento"; //$NON-NLS-1$
	public static final String MEMENTO_ID = "id"; //$NON-NLS-1$
	public static final String MEMENTO_NAME = "name"; //$NON-NLS-1$
	public static final String MEMENTO_URL = "url"; //$NON-NLS-1$


	//
	// Constants used to retrieve images from the cheatsheet image registry.
	//
	public static final String CHEATSHEET_ITEM_SKIP = "CHEATSHEET_ITEM_SKIP"; //$NON-NLS-1$
	public static final String CHEATSHEET_ITEM_COMPLETE = "CHEATSHEET_ITEM_COMPLETE"; //$NON-NLS-1$
	public static final String CHEATSHEET_ITEM_HELP = "CHEATSHEET_ITEM_HELP"; //$NON-NLS-1$
	public static final String CHEATSHEET_START = "CHEATSHEET_START"; //$NON-NLS-1$
	public static final String CHEATSHEET_RESTART = "CHEATSHEET_RESTART"; //$NON-NLS-1$
	public static final String CHEATSHEET_ITEM_BUTTON_START = "CHEATSHEET_ITEM_BUTTON_START"; //$NON-NLS-1$
	public static final String CHEATSHEET_ITEM_BUTTON_SKIP = "CHEATSHEET_ITEM_BUTTON_SKIP"; //$NON-NLS-1$
	public static final String CHEATSHEET_ITEM_BUTTON_COMPLETE = "CHEATSHEET_ITEM_BUTTON_COMPLETE"; //$NON-NLS-1$
	public static final String CHEATSHEET_ITEM_BUTTON_RESTART = "CHEATSHEET_ITEM_BUTTON_RESTART"; //$NON-NLS-1$

	
	//
	// Constants used to retrieve strings from the cheatsheet resource bundle.
	//

	// CheatSheetPlugin
	public static final String ERROR_READING_STATE_FILE = "%ERROR_READING_STATE_FILE"; //$NON-NLS-1$
	public static final String ERROR_WRITING_STATE_FILE = "%ERROR_WRITING_STATE_FILE"; //$NON-NLS-1$

	// CheatSheetCategoryBasedSelectionDialog, CheatSheetSelectionDialog
	public static final String CHEAT_SHEET_SELECTION_DIALOG_TITLE = "%CHEAT_SHEET_SELECTION_DIALOG_TITLE"; //$NON-NLS-1$
	public static final String CHEAT_SHEET_SELECTION_DIALOG_MSG = "%CHEAT_SHEET_SELECTION_DIALOG_MSG"; //$NON-NLS-1$

	// CheatSheetExapndRestoreAction, CheatSheetView
	public static final String COLLAPSE_ALL_BUT_CURRENT_TOOLTIP = "%COLLAPSE_ALL_BUT_CURRENT_TOOLTIP"; //$NON-NLS-1$

	// CheatSheetExapndRestoreAction
	public static final String RESTORE_ALL_TOOLTIP = "%RESTORE_ALL_TOOLTIP"; //$NON-NLS-1$

	// CheatSheetMenu
	public static final String CHEAT_SHEET_OTHER_MENU = "%CHEAT_SHEET_OTHER_MENU"; //$NON-NLS-1$

	// CoreItem
	public static final String PERFORM_TASK_TOOLTIP = "%PERFORM_TASK_TOOLTIP"; //$NON-NLS-1$
	public static final String SKIP_TASK_TOOLTIP = "%SKIP_TASK_TOOLTIP"; //$NON-NLS-1$
	public static final String COMPLETE_TASK_TOOLTIP = "%COMPLETE_TASK_TOOLTIP"; //$NON-NLS-1$
	public static final String RESTART_TASK_TOOLTIP = "%RESTART_TASK_TOOLTIP"; //$NON-NLS-1$
	public static final String LESS_THAN_2_SUBITEMS = "%LESS_THAN_2_SUBITEMS";//$NON-NLS-1$
	public static final String ERROR_FINDING_PLUGIN_FOR_ACTION = "%ERROR_FINDING_PLUGIN_FOR_ACTION"; //$NON-NLS-1$
	public static final String ERROR_DATA_MISSING = "%ERROR_DATA_MISSING"; //$NON-NLS-1$
	public static final String ERROR_DATA_MISSING_LOG = "%ERROR_DATA_MISSING_LOG"; //$NON-NLS-1$
	// IntroItem
	public static final String START_CHEATSHEET_TOOLTIP = "%START_CHEATSHEET_TOOLTIP"; //$NON-NLS-1$
	public static final String RESTART_CHEATSHEET_TOOLTIP = "%RESTART_CHEATSHEET_TOOLTIP"; //$NON-NLS-1$
	// ViewItem
	public static final String HELP_BUTTON_TOOLTIP = "%HELP_BUTTON_TOOLTIP";//$NON-NLS-1$

	// CheatSheetViewer
	public static final String ERROR_RUNNING_ACTION = "%ERROR_RUNNING_ACTION"; //$NON-NLS-1$
	public static final String ERROR_INVALID_CHEATSHEET_ID = "%ERROR_INVALID_CHEATSHEET_ID"; //$NON-NLS-1$
	public static final String ERROR_CHEATSHEET_DOESNOT_EXIST = "%ERROR_CHEATSHEET_DOESNOT_EXIST"; //$NON-NLS-1$
	public static final String INITIAL_VIEW_DIRECTIONS = "%INITIAL_VIEW_DIRECTIONS"; //$NON-NLS-1$

	// ErrorPage
	public static final String ERROR_LOADING_CHEATSHEET_CONTENT = "%ERROR_LOADING_CHEATSHEET_CONTENT"; //$NON-NLS-1$
	public static final String ERROR_PAGE_MESSAGE = "%ERROR_PAGE_MESSAGE"; //$NON-NLS-1$

	// CheatSheetElement, CheatSheetItemExtensionElement, CoreItem
	public static final String ERROR_LOADING_CLASS_FOR_ACTION = "%ERROR_LOADING_CLASS_FOR_ACTION"; //$NON-NLS-1$
	public static final String ERROR_CREATING_CLASS_FOR_ACTION = "%ERROR_CREATING_CLASS_FOR_ACTION"; //$NON-NLS-1$

	// CheatSheetRegistryReader
	public static final String CHEAT_SHEET_OTHER_CATEGORY = "%CHEAT_SHEET_OTHER_CATEGORY"; //$NON-NLS-1$

	// OpenCheatSheetAction
	public static final String LAUNCH_SHEET_ERROR = "%LAUNCH_SHEET_ERROR"; //$NON-NLS-1$
	public static final String CHEAT_SHEET_ERROR_OPENING = "%CHEAT_SHEET_ERROR_OPENING"; //$NON-NLS-1$

	// actions.OpenPerspective
	public static final String ERROR_OPENING_PERSPECTIVE = "%ERROR_OPENING_PERSPECTIVE"; //$NON-NLS-1$

	// CheatSheetSaveHelper
	public static final String ERROR_CREATING_STATEFILE_URL = "%ERROR_CREATING_STATEFILE_URL"; //$NON-NLS-1$
	public static final String ERROR_SAVING_STATEFILE_URL = "%ERROR_SAVING_STATEFILE_URL"; //$NON-NLS-1$
	public static final String ERROR_READING_MANAGERDATA_FROM_STATEFILE = "%ERROR_READING_MANAGERDATA_FROM_STATEFILE"; //$NON-NLS-1$


	// CheatSheetParser, regaular messages
	public static final String CHEAT_SHEET_INTRO_TITLE = "%CHEAT_SHEET_INTRO_TITLE"; //$NON-NLS-1$
	// CheatSheetParser, general error messages
	public static final String ERROR_TITLE = "%ERROR_TITLE"; //$NON-NLS-1$
	// CheatSheetParser - init, file I/O and XML parsing error messages
	public static final String ERROR_CREATING_DOCUMENT_BUILDER = "%ERROR_CREATING_DOCUMENT_BUILDER"; //$NON-NLS-1$
	public static final String ERROR_DOCUMENT_BUILDER_NOT_INIT = "%ERROR_DOCUMENT_BUILDER_NOT_INIT"; //$NON-NLS-1$
	public static final String ERROR_OPENING_FILE = "%ERROR_OPENING_FILE"; //$NON-NLS-1$
	public static final String ERROR_OPENING_FILE_IN_PARSER = "%ERROR_OPENING_FILE_IN_PARSER"; //$NON-NLS-1$
	public static final String ERROR_SAX_PARSING = "%ERROR_SAX_PARSING"; //$NON-NLS-1$
	public static final String ERROR_SAX_PARSING_WITH_LOCATION = "%ERROR_SAX_PARSING_WITH_LOCATION"; //$NON-NLS-1$
	// CheatSheetParser, content error messages
	public static final String ERROR_PARSING_CHEATSHEET_CONTENTS = "%ERROR_PARSING_CHEATSHEET_CONTENTS"; //$NON-NLS-1$
	public static final String ERROR_PARSING_CHEATSHEET_ELEMENT = "%ERROR_PARSING_CHEATSHEET_ELEMENT"; //$NON-NLS-1$
	public static final String ERROR_PARSING_NO_INTRO = "%ERROR_PARSING_NO_INTRO"; //$NON-NLS-1$
	public static final String ERROR_PARSING_MORE_THAN_ONE_INTRO = "%ERROR_PARSING_MORE_THAN_ONE_INTRO"; //$NON-NLS-1$
	public static final String ERROR_PARSING_NO_ITEM = "%ERROR_PARSING_NO_ITEM"; //$NON-NLS-1$
	public static final String ERROR_PARSING_PARAM_INVALIDRANGE = "%ERROR_PARSING_PARAM_INVALIDRANGE"; //$NON-NLS-1$
	public static final String ERROR_PARSING_PARAM_INVALIDNUMBER = "%ERROR_PARSING_PARAM_INVALIDNUMBER"; //$NON-NLS-1$
	public static final String ERROR_PARSING_NO_DESCRIPTION = "%ERROR_PARSING_NO_DESCRIPTION"; //$NON-NLS-1$
	public static final String ERROR_PARSING_NO_SUBITEM = "%ERROR_PARSING_NO_SUBITEM"; //$NON-NLS-1$
	public static final String ERROR_PARSING_NO_ACTION = "%ERROR_PARSING_NO_ACTION"; //$NON-NLS-1$
	public static final String ERROR_PARSING_NO_TITLE = "%ERROR_PARSING_NO_TITLE"; //$NON-NLS-1$
	public static final String ERROR_PARSING_NO_CLASS = "%ERROR_PARSING_NO_CLASS"; //$NON-NLS-1$
	public static final String ERROR_PARSING_NO_PLUGINID = "%ERROR_PARSING_NO_PLUGINID"; //$NON-NLS-1$
	public static final String ERROR_PARSING_NO_CONDITION = "%ERROR_PARSING_NO_CONDITION"; //$NON-NLS-1$
	public static final String ERROR_PARSING_NO_VALUES = "%ERROR_PARSING_NO_VALUES"; //$NON-NLS-1$
	public static final String ERROR_PARSING_NO_LABEL = "%ERROR_PARSING_NO_LABEL"; //$NON-NLS-1$
	// CheatSheetParser, content warning messages
	public static final String WARNING_PARSING_UNKNOWN_ATTRIBUTE = "%WARNING_PARSING_UNKNOWN_ATTRIBUTE"; //$NON-NLS-1$
	public static final String WARNING_PARSING_UNKNOWN_ELEMENT = "%WARNING_PARSING_UNKNOWN_ELEMENT"; //$NON-NLS-1$
	public static final String WARNING_PARSING_DESCRIPTION_UNKNOWN_ELEMENT = "%WARNING_PARSING_DESCRIPTION_UNKNOWN_ELEMENT"; //$NON-NLS-1$
	
}

