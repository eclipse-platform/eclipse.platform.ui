/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *     Robert Roth (robert.roth.off@gmail.com) - Bug 487093: You can too easily clear the search history
 *******************************************************************************/
package org.eclipse.search2.internal.ui;

import org.eclipse.osgi.util.NLS;

public final class SearchMessages extends NLS {

	private static final String BUNDLE_NAME= "org.eclipse.search2.internal.ui.SearchMessages";//$NON-NLS-1$

	private SearchMessages() {
		// Do not instantiate
	}

	static {
		NLS.initializeMessages(BUNDLE_NAME, SearchMessages.class);
	}

	public static String AbstractTextSearchViewPage_update_job_name;
	public static String MatchFilterSelectionAction_label;
	public static String MatchFilterSelectionDialog_description_label;
	public static String MatchFilterSelectionDialog_error_invalid_limit;
	public static String MatchFilterSelectionDialog_filter_description;
	public static String MatchFilterSelectionDialog_label;
	public static String MatchFilterSelectionDialog_limit_description;
	public static String OpenSearchPreferencesAction_label;
	public static String OpenSearchPreferencesAction_tooltip;
	public static String RemoveSelectedMatchesAction_label;
	public static String RemoveSelectedMatchesAction_tooltip;
	public static String SearchAgainAction_label;
	public static String SearchAgainAction_tooltip;
	public static String SearchAgainAction_Error_title;
	public static String SearchAgainAction_Error_message;
	public static String SearchDropDownAction_label;
	public static String SearchDropDownAction_tooltip;
	public static String SearchesDialog_remove_label;
	public static String SearchDropDownAction_running_message;
	public static String Search_Error_openResultView_message;
	public static String Search_Error_openResultView_title;
	public static String SearchHistorySelectionDialog_configure_link_label;
	public static String SearchHistorySelectionDialog_history_size_description;
	public static String SearchHistorySelectionDialog_history_size_error;
	public static String SearchHistorySelectionDialog_history_size_title;
	public static String SearchHistorySelectionDialog_open_in_new_button;
	public static String SearchHistorySelectionDialog_restore_default_button;
	public static String SearchView_empty_search_label;
	public static String ShowSearchesAction_label;
	public static String ShowSearchesAction_tooltip;
	public static String SearchView_showIn_menu;
	public static String SearchesDialog_title;
	public static String SearchesDialog_message;
	public static String RemoveAllSearchesAction_label;
	public static String RemoveAllSearchesAction_tooltip;
	public static String RemoveAllMatchesAction_label;
	public static String RemoveAllMatchesAction_tooltip;
	public static String ShowNextResultAction_label;
	public static String ShowNextResultAction_tooltip;
	public static String ShowPreviousResultAction_label;
	public static String ShowPreviousResultAction_tooltip;
	public static String RemoveMatchAction_label;
	public static String RemoveMatchAction_tooltip;
	public static String DefaultSearchViewPage_show_match;
	public static String DefaultSearchViewPage_error_no_editor;
	public static String AbstractTextSearchViewPage_flat_layout_label;
	public static String AbstractTextSearchViewPage_flat_layout_tooltip;
	public static String AbstractTextSearchViewPage_hierarchical_layout_label;
	public static String AbstractTextSearchViewPage_hierarchical_layout_tooltip;
	public static String CancelSearchAction_label;
	public static String CancelSearchAction_tooltip;
	public static String AbstractTextSearchViewPage_searching_label;
	public static String CollapseAllAction_0;
	public static String CollapseAllAction_1;
	public static String ExpandAllAction_label;
	public static String ExpandAllAction_tooltip;
	public static String SearchView_error_noResultPage;
	public static String InternalSearchUI_error_unexpected;
	public static String NewSearchUI_error_title;
	public static String NewSearchUI_error_label;
	public static String AnnotationHighlighter_error_noDocument;
	public static String EditorAccessHighlighter_error_badLocation;
	public static String PinSearchViewAction_label;
	public static String PinSearchViewAction_tooltip;
	public static String SearchPageRegistry_error_creating_extensionpoint;
	public static String TextSearchGroup_submenu_text;
	public static String FindInWorkspaceActionDelegate_text;
	public static String FindInProjectActionDelegate_text;
	public static String FindInWorkingSetActionDelegate_text;
	public static String FindInFileActionDelegate_text;
	public static String TextSearchQueryProviderRegistry_defaultProviderLabel;
	public static String RetrieverAction_dialog_title;
	public static String RetrieverAction_empty_selection;
	public static String RetrieverAction_error_title;
	public static String RetrieverAction_error_message;
	public static String RemoveAllSearchesAction_confirm_label;
	public static String RemoveAllSearchesAction_confirm_message;

}
