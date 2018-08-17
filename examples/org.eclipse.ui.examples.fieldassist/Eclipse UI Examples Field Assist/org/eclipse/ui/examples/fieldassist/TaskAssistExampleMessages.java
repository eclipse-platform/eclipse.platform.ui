/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.examples.fieldassist;

import org.eclipse.osgi.util.NLS;

/**
 * Message class for the undo example.
 *
 */
public class TaskAssistExampleMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.examples.fieldassist.messages";//$NON-NLS-1$

	public static String Preferences_ContentAssistKey;
	public static String Preferences_ContentAssistKeyPropagate;
	public static String Preferences_ContentAssistResult;
	public static String Preferences_ContentAssistResultReplace;
	public static String Preferences_ContentAssistResultInsert;
	public static String Preferences_ContentAssistResultNone;
	public static String Preferences_ContentAssistFilter;
	public static String Preferences_ContentAssistFilterCumulative;
	public static String Preferences_ContentAssistFilterCharacter;
	public static String Preferences_ContentAssistFilterNone;
	public static String Preferences_ShowSecondaryPopup;
	public static String Preferences_ContentAssistDelay;
	public static String Preferences_ErrorIndicator;
	public static String Preferences_ShowErrorMessage;
	public static String Preferences_ShowErrorDecorator;
	public static String Preferences_ShowWarningDecorator;
	public static String Preferences_ShowProposalCue;
	public static String Preferences_RequiredFieldIndicator;
	public static String Preferences_ShowRequiredFieldDecorator;
	public static String Preferences_ShowRequiredFieldLabelIndicator;
	public static String Preferences_Description;
	public static String Preferences_ContentAssistDescription;
	public static String Preferences_DecoratorDetails;
	public static String Preferences_DecoratorImpl;
	public static String Preferences_DecoratorVert;
	public static String Preferences_DecoratorTop;
	public static String Preferences_DecoratorCenter;
	public static String Preferences_DecoratorBottom;
	public static String Preferences_DecoratorHorz;
	public static String Preferences_DecoratorLeft;
	public static String Preferences_DecoratorRight;
	public static String Preferences_DecoratorMargin;
	public static String Decorator_Warning;
	public static String Decorator_Error;
	public static String Decorator_ContentAssist;

	public static String ExampleDialog_UserError;
	public static String ExampleDialog_WarningName;
	public static String ExampleDialog_UserWarning;
	public static String ExampleDialog_AgeWarning;
	public static String ExampleDialog_Title;
	public static String ExampleDialog_SecurityGroup;
	public static String ExampleDialog_AutoCompleteGroup;
	public static String ExampleDialog_UserName;
	public static String ExampleDialog_ComboUserName;
	public static String ExampleDialog_Age;
	public static String ExampleDialog_Password;
	public static String ExampleDialog_ProposalDescription;
	public static String ExampleDialog_DefaultSelectionTitle;
	public static String ExampleDialog_DefaultSelectionMessage;
	public static String ExampleDialog_SelectionTitle;
	public static String ExampleDialog_SelectionMessage;

	public static String FieldAssistTestDialog_Comments;

	public static String FieldAssistTestDialog_CommentsDefaultContent;
	public static String ExampleDialog_DecorationMenuItem;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, TaskAssistExampleMessages.class);
	}

}
