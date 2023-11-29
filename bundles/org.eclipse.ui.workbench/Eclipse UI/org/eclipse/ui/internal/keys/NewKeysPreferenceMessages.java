/*******************************************************************************
 * Copyright (c) 2006, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - Bug 186522 - [KeyBindings] New Keys preference page does not resort by binding with conflicts
 *     Christian Georgi (SAP SE) - Bug 540440
 *******************************************************************************/

package org.eclipse.ui.internal.keys;

import org.eclipse.osgi.util.NLS;

/**
 * Messages used in the New Keys Preference page.
 *
 * @since 3.2
 */
public class NewKeysPreferenceMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.keys.NewKeysPreferencePage";//$NON-NLS-1$

	public static String AddBindingButton_Text;
	public static String AddKeyButton_ToolTipText;
	public static String FiltersButton_Text;
	public static String ExportButton_Text;
	public static String BindingLabel_Text;
	public static String CommandNameColumn_Text;
	public static String CategoryColumn_Text;
	public static String UserColumn_Text;
	public static String CommandNameLabel_Text;
	public static String CommandDescriptionLabel_Text;
	public static String ConflictsLabel_Text;
	public static String RemoveBindingButton_Text;
	public static String RestoreBindingButton_Text;
	public static String SchemeLabel_Text;
	public static String TriggerSequenceColumn_Text;
	public static String WhenColumn_Text;
	public static String WhenLabel_Text;

	public static String PreferenceStoreError_Message;

	public static String RestoreDefaultsMessageBoxText;
	public static String RestoreDefaultsMessageBoxMessage;

	public static String Undefined_Command;
	public static String Unavailable_Category;

	public static String KeysPreferenceFilterDialog_Title;
	public static String ActionSetFilterCheckBox_Text;
	public static String InternalFilterCheckBox_Text;
	public static String UncategorizedFilterCheckBox_Text;

	public static String ShowCommandKeysGroup_Title;
	public static String ShowCommandKeysForKeyboard_Text;
	public static String ShowCommandKeysForMouse_Text;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, NewKeysPreferenceMessages.class);
	}
}
