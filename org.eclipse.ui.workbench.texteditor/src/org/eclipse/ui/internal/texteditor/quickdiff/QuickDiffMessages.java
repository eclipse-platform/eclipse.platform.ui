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
package org.eclipse.ui.internal.texteditor.quickdiff;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class which wraps the specified resource bundle
 * and offers methods to access the bundle.
 *
 * @since 3.0
 */
public final class QuickDiffMessages extends NLS {

	private static final String BUNDLE_FOR_CONSTRUCTED_KEYS= "org.eclipse.ui.internal.texteditor.quickdiff.ConstructedQuickDiffMessages"; //$NON-NLS-1$
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

	private static final String BUNDLE_NAME= QuickDiffMessages.class.getName();

	private QuickDiffMessages() {
		// Do not instantiate
	}

	public static String quickdiff_toggle_enable;
	public static String quickdiff_toggle_disable;
	public static String quickdiff_initialize;
	public static String quickdiff_nonsynchronized;
	public static String quickdiff_annotation_changed;
	public static String quickdiff_annotation_added;
	public static String quickdiff_annotation_deleted;
	public static String quickdiff_annotation_line_singular;
	public static String quickdiff_annotation_line_plural;
	public static String quickdiff_menu_label;
	public static String quickdiff_error_getting_document_content;
	public static String RestoreAction_label;
	public static String RestoreAction_multiple_label;
	public static String RevertLineAction_label;
	public static String RevertLineAction_delete_label;

	static {
		NLS.initializeMessages(BUNDLE_NAME, QuickDiffMessages.class);
	}
}