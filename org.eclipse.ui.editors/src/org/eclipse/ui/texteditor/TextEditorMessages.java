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
package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 */
final class TextEditorMessages extends NLS {

	private static final String BUNDLE_FOR_CONSTRUCTED_KEYS= "org.eclipse.ui.texteditor.ConstructedTextEditorMessages"; //$NON-NLS-1$
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

	public static String AbstractDecoratedTextEditor_revisions_menu;
	public static String AbstractDecoratedTextEditor_revision_colors_option_by_author;
	public static String AbstractDecoratedTextEditor_revision_colors_option_by_author_and_date;
	public static String AbstractDecoratedTextEditor_revision_colors_option_by_date;
	public static String AbstractDecoratedTextEditor_show_ruler_label;
	public static String SelectMarkerRulerAction_getMarker;
	public static String AddMarkerAction_addMarker;
	public static String MarkerRulerAction_getMarker;
	public static String AbstractMarkerAnnotationModel_connected;
	public static String AbstractMarkerAnnotationModel_createMarkerUpdater;
	public static String AbstractMarkerAnnotationModel_removeAnnotations;
	public static String DocumentProviderRegistry_error_extension_point_not_found;
	public static String ChangeEncodingAction_message_noEncodingSupport;
	public static String ChangeEncodingAction_button_apply_label;

	public static String AbstractDecoratedTextEditor_warning_saveAs_deleted;
	public static String AbstractDecoratedTextEditor_error_saveAs_title;
	public static String AbstractDecoratedTextEditor_error_saveAs_message;

	public static String AbstractDecoratedTextEditor_save_error_Dialog_button_saveAsUTF8;

	public static String AbstractDecoratedTextEditor_save_error_Dialog_button_selectUnmappable;
	public static String AbstractDecoratedTextEditor_saveAs_overwrite_title;
	public static String AbstractDecoratedTextEditor_saveAs_overwrite_message;
	public static String AbstractDecoratedTextEditor_warning_derived_title;
	public static String AbstractDecoratedTextEditor_warning_derived_message;
	public static String AbstractDecoratedTextEditor_warning_derived_dontShowAgain;

	public static String AbstractDecoratedTextEditor_openWith_menu;
	public static String AbstractDecoratedTextEditor_showIn_menu;
	public static String AbstractDecoratedTextEditor_printPageNumber;


	static {
		NLS.initializeMessages(BUNDLE_NAME, TextEditorMessages.class);
	}

}
