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
package org.eclipse.ui.texteditor.templates;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 *
 * @since 3.0
 */
final class TextEditorTemplateMessages extends NLS {

	private static final String BUNDLE_NAME= TextEditorTemplateMessages.class.getName();

	private TextEditorTemplateMessages() {
		// Do not instantiate
	}

	public static String TemplatePreferencePage_error_import;
	public static String TemplatePreferencePage_error_export;
	public static String TemplatePreferencePage_error_read_title;
	public static String TemplatePreferencePage_error_write_title;
	public static String TemplatePreferencePage_message;
	public static String TemplatePreferencePage_title;
	public static String TemplatePreferencePage_new;
	public static String TemplatePreferencePage_edit;
	public static String TemplatePreferencePage_import;
	public static String TemplatePreferencePage_export;
	public static String TemplatePreferencePage_remove;
	public static String TemplatePreferencePage_editor;
	public static String TemplatePreferencePage_revert;
	public static String TemplatePreferencePage_restore;
	public static String TemplatePreferencePage_column_name;
	public static String TemplatePreferencePage_column_context;
	public static String TemplatePreferencePage_column_description;
	public static String TemplatePreferencePage_on;
	public static String TemplatePreferencePage_use_code_formatter;
	public static String TemplatePreferencePage_import_title;
	public static String TemplatePreferencePage_import_extension;
	public static String TemplatePreferencePage_export_title;
	public static String TemplatePreferencePage_export_filename;
	public static String TemplatePreferencePage_export_extension;
	public static String TemplatePreferencePage_export_exists_title;
	public static String TemplatePreferencePage_export_exists_message;
	public static String TemplatePreferencePage_export_error_title;
	public static String TemplatePreferencePage_export_error_hidden;
	public static String TemplatePreferencePage_export_error_canNotWrite;
	public static String TemplatePreferencePage_export_error_fileNotFound;
	public static String TemplatePreferencePage_error_parse_message;
	public static String TemplatePreferencePage_error_read_message;
	public static String TemplatePreferencePage_error_write_message;
	public static String TemplatePreferencePage_question_create_new_title;
	public static String TemplatePreferencePage_question_create_new_message;
	public static String TemplatePreferencePage_preview;
	public static String EditTemplateDialog_error_noname;
	public static String EditTemplateDialog_error_adjacent_variables;
	public static String EditTemplateDialog_title_new;
	public static String EditTemplateDialog_title_edit;
	public static String EditTemplateDialog_name;
	public static String EditTemplateDialog_description;
	public static String EditTemplateDialog_context;
	public static String EditTemplateDialog_pattern;
	public static String EditTemplateDialog_insert_variable;
	public static String EditTemplateDialog_undo;
	public static String EditTemplateDialog_cut;
	public static String EditTemplateDialog_copy;
	public static String EditTemplateDialog_paste;
	public static String EditTemplateDialog_select_all;
	public static String EditTemplateDialog_content_assist;
	public static String TemplateVariableProposal_error_title;
	public static String TemplateVariableProcessor_error_title;
	public static String GlobalVariables_variable_description_cursor;
	public static String GlobalVariables_variable_description_dollar;
	public static String GlobalVariables_variable_description_date;
	public static String GlobalVariables_variable_description_year;
	public static String GlobalVariables_variable_description_time;
	public static String GlobalVariables_variable_description_user;
	public static String GlobalVariables_variable_description_selectedWord;
	public static String GlobalVariables_variable_description_selectedLines;
	public static String TemplatePreferencePage_column_autoinsert;
	public static String EditTemplateDialog_autoinsert;

	static {
		NLS.initializeMessages(BUNDLE_NAME, TextEditorTemplateMessages.class);
	}

}