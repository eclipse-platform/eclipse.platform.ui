/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.keys;

import org.eclipse.osgi.util.NLS;

/**
 * Messages used in the New Keys Preference page.
 * 
 * @since 3.2
 *
 */
public class NewKeysPreferenceMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.keys.NewKeysPreferencePage";//$NON-NLS-1$
	
	public static String AddBindingButton_Text;
	public static String AddKeyButton_ToolTipText;
	public static String AdvancedButton_Text;
	public static String BindingLabel_Text;
	public static String CommandNameColumn_Text;
	public static String CommandNameLabel_Text;
	public static String DeleteSchemeButton_Text;
	public static String DescriptionLabel_Text;
	public static String RemoveBindingButton_Text;
	public static String SchemeLabel_Text;
	public static String ShowAllCheckBox_Text;
	public static String TriggerSequenceColumn_Text;
	public static String WhenLabel_Text;
	
	public static String GroupingCombo_Label;
	public static String GroupingCombo_Category_Text;
	public static String GroupingCombo_None_Text;
	public static String GroupingCombo_When_Text;
	
	public static String PreferenceStoreError_Message;
	public static String PreferenceStoreError_Title;
	
	public static String RestoreDefaultsMessageBoxText;
	public static String RestoreDefaultsMessageBoxMessage;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, NewKeysPreferenceMessages.class);
	}
}
