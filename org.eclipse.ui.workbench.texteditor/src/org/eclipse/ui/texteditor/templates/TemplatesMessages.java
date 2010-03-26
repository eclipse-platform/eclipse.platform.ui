/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dakshinamurthy Karra (Jalian Systems) - Templates View - https://bugs.eclipse.org/bugs/show_bug.cgi?id=69581
 *******************************************************************************/
package org.eclipse.ui.texteditor.templates;

import org.eclipse.osgi.util.NLS;


/**
 * Helper class to get NLSed messages.
 *
 * @since 3.0
 */
final class TemplatesMessages extends NLS {

	private static final String BUNDLE_NAME= TemplatesMessages.class.getName();

	private TemplatesMessages() {
		// Do not instantiate
	}

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
	public static String EditTemplateDialog_error_invalidPattern;
	public static String EditTemplateDialog_title_new;
	public static String EditTemplateDialog_title_edit;
	public static String EditTemplateDialog_name;
	public static String EditTemplateDialog_description;
	public static String EditTemplateDialog_context;
	public static String EditTemplateDialog_pattern;
	public static String EditTemplateDialog_insert_variable;
	public static String EditTemplateDialog_undo;
	public static String EditTemplateDialog_redo;
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
	public static String TemplatesPage_column_context;
	public static String TemplatesPage_column_description;
	public static String TemplatesPage_column_name;
	public static String TemplatesPage_insert;
	public static String TemplatesPage_edit;
	public static String TemplatesPage_copy;
	public static String TemplatesPage_paste;
	public static String TemplatesPage_preference_page;
	public static String TemplatesPage_link_to_editor;
	public static String TemplatesPage_collapse_all;
	public static String TemplatesPage_new;
	public static String TemplatesPage_remove;
	public static String TemplatesPage_insert_tooltip;
	public static String TemplatesPage_edit_tooltip;
	public static String TemplatesPage_preference_page_tooltip;
	public static String TemplatesPage_link_to_editor_tooltip;
	public static String TemplatesPage_collapse_all_tooltip;
	public static String TemplatesPage_new_tooltip;
	public static String TemplatesPage_remove_tooltip;
	public static String TemplatesPage_preview;
	public static String TemplatesPage_question_create_new_message;
	public static String TemplatesPage_question_create_new_title;
	public static String TemplatesPage_save_error_message;
	public static String TemplatesPage_snippet;
	public static String TemplatesPage_paste_description;
	public static String TemplatesPage_remove_message_single;
	public static String TemplatesPage_remove_message_multi;
	public static String TemplatesPage_remove_title_single;
	public static String TemplatesPage_remove_title_multi;
	public static String TemplatesView_no_templates;

	static {
		NLS.initializeMessages(BUNDLE_NAME, TemplatesMessages.class);
	}

}