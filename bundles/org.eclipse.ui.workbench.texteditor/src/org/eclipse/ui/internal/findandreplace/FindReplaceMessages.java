/*******************************************************************************
 * Copyright (c) 2023 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.findandreplace;

import org.eclipse.osgi.util.NLS;

final public class FindReplaceMessages extends NLS {

	public static final String BUNDLE_NAME = FindReplaceMessages.class.getName();

	private FindReplaceMessages() {
		// Do not instantiate
	}

	static {
		NLS.initializeMessages(BUNDLE_NAME, FindReplaceMessages.class);
	}

	public static String FindReplace_Status_replacement_label;
	public static String FindReplace_Status_replacements_label;
	public static String FindReplace_Status_noMatchWithValue_label;
	public static String FindReplace_Status_wrapped_label;
	public static String FindReplaceDialog_read_only;
	public static String FindReplace_Status_selections_label;
	public static String FindReplace_Status_selection_label;
	public static String FindReplace_Status_noMatch_label;

	// The "classic" Find/Replace-Dialog
	public static String FindReplace_Dialog_Title;
	public static String FindReplace_Find_label;
	public static String FindReplace_Replace_label;
	public static String FindReplace_Direction;
	public static String FindReplace_ForwardRadioButton_label;
	public static String FindReplace_BackwardRadioButton_label;
	public static String FindReplace_Scope;
	public static String FindReplace_GlobalRadioButton_label;
	public static String FindReplace_SelectedRangeRadioButton_label;
	public static String FindReplace_Options;
	public static String FindReplace_CaseCheckBox_label;
	public static String FindReplace_WrapCheckBox_label;
	public static String FindReplace_WholeWordCheckBox_label;
	public static String FindReplace_IncrementalCheckBox_label;
	public static String FindReplace_RegExCheckbox_label;
	public static String FindReplace_FindNextButton_label;
	public static String FindReplace_ReplaceFindButton_label;
	public static String FindReplace_ReplaceSelectionButton_label;
	public static String FindReplace_ReplaceAllButton_label;
	public static String FindReplace_SelectAllButton_label;
	public static String FindReplace_CloseButton_label;

	public static String FindReplaceOverlay_downSearchButton_toolTip;
	public static String FindReplaceOverlay_upSearchButton_toolTip;
	public static String FindReplaceOverlay_searchAllButton_toolTip;
	public static String FindReplaceOverlay_searchInSelectionButton_toolTip;
	public static String FindReplaceOverlay_regexSearchButton_toolTip;
	public static String FindReplaceOverlay_caseSensitiveButton_toolTip;
	public static String FindReplaceOverlay_wholeWordsButton_toolTip;
	public static String FindReplaceOverlay_replaceButton_toolTip;
	public static String FindReplaceOverlay_replaceAllButton_toolTip;
	public static String FindReplaceOverlay_searchBar_message;
	public static String FindReplaceOverlay_replaceBar_message;
	public static String FindReplaceOverlay_replaceToggle_toolTip;
	public static String FindReplaceOverlayFirstTimePopup_FindReplaceOverlayFirstTimePopup_message;
	public static String FindReplaceOverlayFirstTimePopup_FindReplaceOverlayFirstTimePopup_title;
}
