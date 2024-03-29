/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation.
 *     Pierre-Yves B., pyvesdev@gmail.com - Bug 121634: [find/replace] status bar must show the string being searched when "String Not Found"
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;


/**
 * Helper class to get NLSed messages.
 */
final class EditorMessages extends NLS {

	private static final String BUNDLE_FOR_CONSTRUCTED_KEYS= "org.eclipse.ui.texteditor.ConstructedEditorMessages";//$NON-NLS-1$
	private static ResourceBundle fgBundleForConstructedKeys= ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS);

	/**
	 * Returns the message bundle which contains constructed keys.
	 *
	 * @since 3.1
	 * @return the message bundle
	 */
	public static ResourceBundle getBundleForConstructedKeys() {
		return fgBundleForConstructedKeys;
	}

	private static final String BUNDLE_NAME= EditorMessages.class.getName();

	private EditorMessages() {
		// Do not instantiate
	}

	public static String Editor_error_no_provider;
	public static String Editor_error_save_title;
	public static String Editor_error_save_message;
	public static String Editor_error_save_deleted_title;
	public static String Editor_error_save_deleted_message;
	public static String Editor_error_init;
	public static String Editor_error_save_outofsync_title;
	public static String Editor_error_save_outofsync_message;
	public static String Editor_error_activated_outofsync_title;
	public static String Editor_error_activated_outofsync_message;
	public static String Editor_error_replace_button_label;
	public static String Editor_error_dontreplace_button_label;
	public static String Editor_error_activated_deleted_save_title;
	public static String Editor_error_activated_deleted_save_message;
	public static String Editor_error_activated_deleted_save_button_save;
	public static String Editor_error_activated_deleted_save_button_close;
	public static String Editor_error_activated_deleted_close_title;
	public static String Editor_error_activated_deleted_close_message;
	public static String Editor_error_refresh_outofsync_title;
	public static String Editor_error_refresh_outofsync_message;
	public static String Editor_error_revert_title;
	public static String Editor_error_revert_message;
	public static String Editor_error_setinput_title;
	public static String Editor_error_setinput_message;
	public static String Editor_error_validateEdit_title;
	public static String Editor_error_validateEdit_message;
	public static String Editor_error_HyperlinkDetector_couldNotCreate_message;
	public static String Editor_error_HyperlinkDetector_invalidElementName_message;
	public static String Editor_error_HyperlinkDetector_invalidExtension_message;
	public static String Editor_error_HyperlinkDetectorTarget_invalidElementName_message;
	public static String Editor_error_HyperlinkDetectorTarget_invalidExtension_message;
	public static String AbstractDocumentProvider_error_save_inuse;
	public static String Editor_mark_status_message_mark_set;
	public static String Editor_mark_status_message_mark_cleared;
	public static String Editor_mark_status_message_mark_swapped;
	public static String MarkRegionTarget_markNotSet;
	public static String MarkRegionTarget_markNotVisible;
	public static String Editor_FindIncremental_name;
	public static String Editor_FindIncremental_reverse_name;
	public static String Editor_FindIncremental_not_found_pattern;
	public static String Editor_FindIncremental_found_pattern;
	public static String Editor_FindIncremental_render_tab;
	public static String Editor_FindIncremental_wrapped;
	public static String Editor_FindIncremental_reverse;
	public static String Editor_ConvertLineDelimiter_title;
	public static String Editor_statusline_state_readonly_label;
	public static String Editor_statusline_state_writable_label;
	public static String Editor_statusline_mode_insert_label;
	public static String Editor_statusline_mode_overwrite_label;
	public static String Editor_statusline_mode_smartinsert_label;
	public static String Editor_statusline_position_pattern;
	public static String Editor_statusline_position_pattern_offset;
	public static String Editor_statusline_position_pattern_selection;
	public static String Editor_statusline_error_label;
	public static String WorkbenchChainedTextFontFieldEditor_defaultWorkbenchTextFont;

	public static String FindNext_Status_noMatch_label;
	public static String AbstractDocumentProvider_ok;
	public static String AbstractDocumentProvider_error;
	public static String FindReplaceDialog_read_only;
	public static String Editor_MoveLines_IllegalMove_status;
	public static String Editor_error_clipboard_copy_failed_message;
	public static String Editor_font_reset_message;
	public static String Editor_font_zoom_message;

	static {
		NLS.initializeMessages(BUNDLE_NAME, EditorMessages.class);
	}
}