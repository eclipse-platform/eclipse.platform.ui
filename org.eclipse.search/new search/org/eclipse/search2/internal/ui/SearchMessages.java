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
	public static String SearchView_refresh_progress_job_label;
	public static String SearchView_update_title_job_name;
	public static String Search_Error_openResultView_title;
	public static String ShowSearchesAction_label;
	public static String ShowSearchesAction_tooltip;
	public static String ShowSearchesAction_dialog_title;
	public static String ShowSearchesAction_dialog_message;
	public static String SearchView_empty_message;
	public static String SearchView_title_search;
	public static String SearchView_showIn_menu;
	public static String SearchesDialog_title;
	public static String SearchesDialog_message;
	public static String RemoveAllSearchesAction_label;
	public static String RemoveAllSearchesAction_tooltip;
	public static String RemoveAllMatchesAction_label;
	public static String RemoveAllMatchesAction_tooltip;
	public static String ShowNextResultAction_label;
	public static String ShowNextResultAction_tooltip;
	public static String SortDropDownActon_label;
	public static String SortDropDownActon_tooltip;
	public static String SortDropDownActon_ascending_label;
	public static String SortDropDownActon_descending_label;
	public static String SortDropDownActon_nosort_label;
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
	public static String AnnotationHighlighter_error_badLocation;
	public static String AnnotationHighlighter_error_noDocument;
	public static String EditorAccessHighlighter_error_badLocation;
	public static String SearchHistoryDropDownAction_showemptyview_title;
	public static String SearchHistoryDropDownAction_showemptyview_tooltip;
	public static String PinSearchViewAction_label;
	public static String PinSearchViewAction_tooltip;
	public static String SearchPageRegistry_error_creating_extensionpoint;
    public static String RetrieverFindTab_search;
    public static String RetrieverFindTab_caseSensitive;
    public static String RetrieverFindTab_regularExpression;
    public static String RetrieverFindTab_wholeWord;
    public static String RetrieverFindTab_searchScope;
    public static String RetrieverFindTab_choose;
    public static String RetrieverFindTab_filePatterns;
    public static String RetrieverFindTab_Error_emptySearchString;
    public static String RetrieverFindTab_Error_invalidRegex;
    public static String RetrieverFindTab_Question_regexMatchesEmptyString;
    public static String RetrieverQuery_label;
    public static String RetrieverResult_label;
    public static String RetrieverResult_noInput_label;
    public static String FilePatternSelectionDialog_title;
    public static String FilePatternSelectionDialog_message;
    public static String FilePatternSelectionDialog_selectAll;
    public static String FilePatternSelectionDialog_deselectAll;
    public static String WorkspaceScopeDescription_label;
    public static String RetrieverFilterTab_LocationFilter_text;
    public static String RetrieverFilterTab_Comment_text;
    public static String RetrieverFilterTab_Import_text;
    public static String RetrieverFilterTab_Preprocessor_text;
    public static String RetrieverFilterTab_String_text;
    public static String RetrieverFilterTab_OtherLocation_text;
    public static String RetrieverFilterTab_TextFilter_text;
    public static String RetrieverFilterTab_HideMatching_text;
    public static String WindowWorkingSetScopeDescription_label;
    public static String RetrieverReplaceTab_ReplaceWith_label;
    public static String RetrieverReplaceTab_Find_text;
    public static String RetrieverReplaceTab_ReplaceFind_text;
    public static String RetrieverReplaceTab_RestoreFind_text;
    public static String RetrieverReplaceTab_Replace_text;
    public static String RetrieverReplaceTab_ReplaceAll_text;
    public static String RetrieverReplaceTab_RestoreAll_text;
    public static String RetrieverReplaceTab_Preview_label;
    public static String RetrieverReplaceTab_Restore_text;
    public static String RetrieverReplaceTab_ReplaceWith_text;
    public static String RetrieverLabelProvider_FilterHidesMatches_label;
    public static String RetrieverFilterTab_FunctionBody_text;
    public static String RetrieverPage_FindTab_text;
    public static String RetrieverPage_FilterTab_text;
    public static String RetrieverPage_ReplaceTab_text;
    public static String RetrieverPage_EnableFilter_text;
    public static String RetrieverPage_EnableFilter_tooltip;
    public static String RetrieverPage_LoadQuery_text;
    public static String RetrieverPage_LoadQuery_tooltip;
    public static String RetrieverPage_SaveQuery_text;
    public static String RetrieverPage_SaveQuery_tooltip;
    public static String RetrieverPage_CreateWorkingSet_text;
    public static String RetrieverPage_CreateWorkingSet_tooltip;
    public static String RetrieverPage_CaseSensitiveFilePatterns_text;
    public static String RetrieverPage_error_noResourcesForWorkingSet;
    public static String RetrieverPage_CreateWorkingsetDialog_title;
    public static String RetrieverPage_CreateWorkingSetDialog_description;
    public static String RetrieverPage_question_overwriteWorkingSet;
    public static String RetrieverPage_error_cannotLoadQuery;
    public static String RetrieverPage_error_cannotStoreQuery;
    public static String RetrieverPage_ErrorDialog_title;
    public static String RetrieverPage_QuestionDialog_title;
    public static String RetrieverPage_InformationDialog_title;
    public static String RetrieverPage_ConsiderDerived_text;
    public static String ReplaceOperation_error_cannotLocateMatch;
    public static String ReplaceOperation_error_operationFailed;
    public static String ReplaceOperation_task_performChanges;
    public static String ReplaceOperation_error_didNotSucceedForAllMatches;
    public static String ReplaceOperation_error_multipleErrors;
    public static String ReplaceOperation_error_whileRefreshing;
    public static String ReplaceOperation_error_whileValidateEdit;
    public static String ReplaceOperation_error_allFilesReadOnly;
    public static String ReplaceOperation_question_continueWithReadOnly_singular;
    public static String ReplaceOperation_question_continueWithReadOnly_plural;
    public static String ReplaceOperation_error_cannotComputeReplacement;
    public static String ReplaceOperation_error_cannotLocateMatchAt;
    public static String RetrieverReplaceTab_ReplaceSelected_text;
    public static String RetrieverReplaceTab_RestoreSelected_text;
    public static String TextFileScannerRegistry_error_instanciateScanner;
    public static String CurrentFileScopeDescription_label;
    public static String CurrentProjectScopeDescription_label;
    public static String TextSearchGroup_submenu_text;
    public static String FindInWorkspaceActionDelegate_text;
    public static String FindInRecentScopeActionDelegate_text;
    public static String FindInProjectActionDelegate_text;
    public static String FindInWorkingSetActionDelegate_text;
    public static String FindInFileActionDelegate_text;

}