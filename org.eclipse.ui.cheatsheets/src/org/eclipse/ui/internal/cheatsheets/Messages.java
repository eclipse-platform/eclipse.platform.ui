/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.cheatsheets.Messages";//$NON-NLS-1$

	private Messages() {
		// Do not instantiate
	}

	public static String ERROR_READING_STATE_FILE;
	public static String ERROR_WRITING_STATE_FILE;
	public static String CHEAT_SHEET_SELECTION_DIALOG_TITLE;
	public static String CHEAT_SHEET_SELECTION_DIALOG_MSG;
	public static String COLLAPSE_ALL_BUT_CURRENT_TOOLTIP;
	public static String CATEGORY_OTHER;
	public static String RESTORE_ALL_TOOLTIP;
	public static String CHEAT_SHEET_OTHER_MENU;
	public static String PERFORM_TASK_TOOLTIP;
	public static String SKIP_TASK_TOOLTIP;
	public static String COMPLETE_TASK_TOOLTIP;
	public static String RESTART_TASK_TOOLTIP;
	public static String LESS_THAN_2_SUBITEMS;
	public static String ERROR_FINDING_PLUGIN_FOR_ACTION;
	public static String ERROR_DATA_MISSING;
	public static String ERROR_DATA_MISSING_LOG;
	public static String ERROR_CONDITIONAL_DATA_MISSING_LOG;
	public static String ERROR_LOADING_CLASS_FOR_ACTION;
	public static String ERROR_CREATING_CLASS_FOR_ACTION;
	public static String START_CHEATSHEET_TOOLTIP;
	public static String RESTART_CHEATSHEET_TOOLTIP;
	public static String HELP_BUTTON_TOOLTIP;
	public static String ERROR_RUNNING_ACTION;
	public static String ERROR_INVALID_CHEATSHEET_ID;
	public static String ERROR_CHEATSHEET_DOESNOT_EXIST;
	public static String ERROR_APPLYING_STATE_DATA;
	public static String ERROR_APPLYING_STATE_DATA_LOG;
	public static String INITIAL_VIEW_DIRECTIONS;
	public static String ERROR_LOADING_CHEATSHEET_CONTENT;
	public static String ERROR_PAGE_MESSAGE;
	public static String ERROR_LOADING_CLASS;
	public static String ERROR_CREATING_CLASS;
	public static String CHEAT_SHEET_OTHER_CATEGORY;
	public static String LAUNCH_SHEET_ERROR;
	public static String CHEAT_SHEET_ERROR_OPENING;
	public static String ERROR_OPENING_PERSPECTIVE;
	public static String ERROR_CREATING_STATEFILE_URL;
	public static String ERROR_SAVING_STATEFILE_URL;
	public static String ERROR_READING_MANAGERDATA_FROM_STATEFILE;
	public static String CHEAT_SHEET_INTRO_TITLE;
	public static String ERROR_TITLE;
	public static String ERROR_CREATING_DOCUMENT_BUILDER;
	public static String ERROR_DOCUMENT_BUILDER_NOT_INIT;
	public static String ERROR_OPENING_FILE;
	public static String ERROR_OPENING_FILE_IN_PARSER;
	public static String ERROR_SAX_PARSING;
	public static String ERROR_SAX_PARSING_WITH_LOCATION;
	public static String ERROR_PARSING_CHEATSHEET_CONTENTS;
	public static String ERROR_PARSING_CHEATSHEET_ELEMENT;
	public static String ERROR_PARSING_NO_INTRO;
	public static String ERROR_PARSING_MORE_THAN_ONE_INTRO;
	public static String ERROR_PARSING_NO_ITEM;
	public static String ERROR_PARSING_PARAM_INVALIDRANGE;
	public static String ERROR_PARSING_PARAM_INVALIDNUMBER;
	public static String ERROR_PARSING_NO_DESCRIPTION;
	public static String ERROR_PARSING_NO_SUBITEM;
	public static String ERROR_PARSING_NO_ACTION;
	public static String ERROR_PARSING_NO_TITLE;
	public static String ERROR_PARSING_NO_CLASS;
	public static String ERROR_PARSING_NO_PLUGINID;
	public static String ERROR_PARSING_NO_CONDITION;
	public static String ERROR_PARSING_NO_VALUES;
	public static String ERROR_PARSING_NO_LABEL;
	public static String ERROR_PARSING_NO_SERIALIZATION;
	public static String ERROR_COMMAND_ID_NOT_FOUND;
	public static String ERROR_COMMAND_ERROR_STATUS;
	public static String ERROR_COMMAND_SERVICE_UNAVAILABLE;
	public static String WARNING_PARSING_UNKNOWN_ATTRIBUTE;
	public static String WARNING_PARSING_UNKNOWN_ELEMENT;
	public static String WARNING_PARSING_DESCRIPTION_UNKNOWN_ELEMENT;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String CheatSheetCategoryBasedSelectionDialog_showAll;
}