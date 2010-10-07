/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.text;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;


/**
 * Helper class to get NLSed messages.
 */
final class TextEditorMessages extends NLS {

	private static final String BUNDLE_FOR_CONSTRUCTED_KEYS= "org.eclipse.ui.editors.text.ConstructedTextEditorMessages";//$NON-NLS-1$
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

	private static final String BUNDLE_NAME= TextEditorMessages.class.getName();

	private TextEditorMessages() {
		// Do not instantiate
	}

	public static String PreferencePage_description;
	public static String PreferencePage_fontEditor;
	public static String FileDocumentProvider_createElementInfo;
	public static String FileDocumentProvider_error_out_of_sync;
	public static String FileDocumentProvider_task_saving;
	public static String FileDocumentProvider_resetDocument;
	public static String FileDocumentProvider_getPersistedEncoding;
	public static String FileDocumentProvider_warning_fileIsDerived;
	public static String FileDocumentProvider_stateValidationFailed;
	public static String DocumentProvider_error_charset_mapping_failed_message_arg;
	public static String DocumentProvider_error_illegal_encoding_message_arg;
	public static String DocumentProvider_error_unsupported_encoding_message_arg;
	public static String StorageDocumentProvider_updateCache;
	public static String StorageDocumentProvider_isReadOnly;
	public static String StorageDocumentProvider_isModifiable;
	public static String StorageDocumentProvider_getContentDescriptionFor;
	public static String StorageDocumentProvider_getContentDescription;
	public static String TextFileDocumentProvider_beginTask_saving;
	public static String TextFileDocumentProvider_error_doesNotExist;
	public static String TextFileDocumentProvider_error_outOfSyncHint;
	public static String TextFileDocumentProvider_error_outOfSyncHintWithKeyBinding;
	public static String TextFileDocumentProvider_saveAsTargetOpenInEditor;
	public static String Editor_error_unreadable_encoding_header;
	public static String Editor_error_unreadable_encoding_banner;
	public static String Editor_error_unreadable_encoding_message_arg;
	public static String Editor_error_unreadable_encoding_message;
	public static String Editor_error_unsupported_encoding_header;
	public static String Editor_error_unsupported_encoding_banner;
	public static String Editor_error_unsupported_encoding_message_arg;
	public static String Editor_error_unsupported_encoding_message;
	public static String Editor_ConvertEncoding_submenu_label;
	public static String Editor_ConvertEncoding_Custom_dialog_title;
	public static String Editor_ConvertEncoding_Custom_dialog_message;
	public static String Editor_toolTip_affordance;
	public static String NullProvider_error;
	public static String FileBufferOperationAction_collectionFiles_label;
	public static String FileBufferOperationHandler_collectionFiles_label;
	public static String ResourceInfo_fileContentEncodingFormat;
	public static String ResourceInfo_fileContainerEncodingFormat;
	public static String WorkbenchPreference_encoding_BOM_UTF_8;
	public static String WorkbenchPreference_encoding_BOM_UTF_16BE;
	public static String WorkbenchPreference_encoding_BOM_UTF_16LE;
	public static String DocumentInputStream_error_streamClosed;

	static {
		NLS.initializeMessages(BUNDLE_NAME, TextEditorMessages.class);
	}
}
