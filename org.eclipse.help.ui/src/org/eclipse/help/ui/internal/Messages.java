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
package org.eclipse.help.ui.internal;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.help.ui.internal.Messages";//$NON-NLS-1$

	private Messages() {
		// Do not instantiate
	}

	public static String Help_Error;
	public static String Help_Question;
	public static String Help_Info;
	public static String browserTitle;
	public static String use_only_external_browser;
	public static String select_browser;
	public static String current_browser;
	public static String SystemBrowser_noProgramForURL;
	public static String SystemBrowser_noProgramForHTML;
	public static String CustomBrowserPreferencePage_Program;
	public static String CustomBrowserPreferencePage_Browse;
	public static String CustomBrowserPreferencePage_Details;
	public static String AppserverPreferencePage_description;
	public static String AppserverPreferencePage_hostDescription;
	public static String AppserverPreferencePage_portDescription;
	public static String AppserverPreferencePage_requireRestart;
	public static String AppserverPreferencePage_invalidPort;
	public static String WW002;
	public static String WE022;
	public static String WE023;
	public static String WE024;
	public static String WE029;
	public static String WW003;
	public static String LaunchURL_exception;
	public static String ReusableHelpPart_searchPage_name;
	public static String ReusableHelpPart_allTopicsPage_name;
	public static String ReusableHelpPart_bookmarksPage_name;
	public static String ReusableHelpPart_contextHelpPage_name;
	public static String ReusableHelpPart_back_label;
	public static String ReusableHelpPart_back_tooltip;
	public static String ReusableHelpPart_forward_label;
	public static String ReusableHelpPart_forward_tooltip;
	public static String ReusableHelpPart_openInfoCenterAction_label;
	public static String ReusableHelpPart_openAction_label;
	public static String ReusableHelpPart_openInHelpContentsAction_label;
	public static String ReusableHelpPart_copyAction_label;
	public static String ReusableHelpPart_bookmarkAction_label;
	public static String ReusableHelpPart_status;
	public static String HelpView_defaultText;
	public static String expression;
	public static String expression_label;
	public static String limit_to;
	public static String SeeAlsoPart_search;
	public static String SeeAlsoPart_allTopics;
	public static String SeeAlsoPart_goto;
	public static String SeeAlsoPart_contextHelp;
	public static String SeeAlsoPart_bookmarks;
	public static String ContextHelpPart_about;
	public static String ContextHelpPart_aboutP;
	public static String ContextHelpPart_dynamicHelp;
	public static String ContextHelpPart_query_view;
	public static String ContextHelpPart_query_perspective;
	public static String ContextHelpPart_seeAlso;
	public static String SearchResultsPart_label;
	public static String SearchResultsPart_progress;
	public static String SearchResultsPart_cancel;
	public static String SearchResultsPart_nwtooltip;
	public static String SearchResultsPart_bmktooltip;
	public static String SearchResultsPart_moreResults;
	public static String SearchPart_clearResults;
	public static String SearchPart_dynamicJob;
	public static String SearchPart_learnMore;
	public static String SearchPart_title;
	public static String SearchPart_stop;
	public static String SearchPart_go;
	public static String SearchPart_errors;
	public static String SearchPart_collapse;
	public static String SearchPart_expand;
	public static String WorkingSetPageDescription;
	public static String WorkingSetContent;
	public static String selectAll;
	public static String selectWorkingSet;
	public static String WE030;
	public static String WE031;
	public static String WE032;
	public static String WE033;
	public static String FederatedSearchPart_advanced;
	public static String FederatedSearchPart_changeScopeSet;
	public static String ScopeSet_default;
	public static String ScopeSetDialog_wtitle;
	public static String ScopeSetDialog_new;
	public static String ScopeSetDialog_edit;
	public static String ScopeSetDialog_remove;
	public static String ScopeSetDialog_rename;
	public static String RenameDialog_wtitle;
	public static String RenameDialog_label;
	public static String RenameDialog_validationError;
	public static String EngineResultSection_moreResults;
	public static String EngineResultSection_lessResults;
	public static String EngineResultSection_progress2;
	public static String EngineResultSection_canceling;
	public static String EngineResultSection_progressError;
	public static String EngineResultSection_previous;
	public static String EngineResultSection_next;
	public static String EngineResultSection_sectionTitle_error;
	public static String EngineResultSection_sectionTitle_hit;
	public static String EngineResultSection_progressTooltip;
	public static String EngineResultSection_searchInProgress;
	public static String EngineResultSection_sectionTitle_hits;
	public static String EngineResultSection_sectionTitle_hitsRange;
	public static String EngineTypeWizardPage_title;
	public static String EngineTypeWizardPage_desc;
	public static String EngineTypeWizardPage_label;
	public static String EngineDescriptorManager_errorLoading;
	public static String EngineDescriptorManager_errorSaving;
	public static String SearchResultsPart_showCategoriesAction_tooltip;
	public static String SearchResultsPart_showDescriptionAction_tooltip;
	public static String ScopePreferenceDialog_wtitle;
	public static String ScopePreferenceDialog_new;
	public static String ScopePreferenceDialog_delete;
	public static String NewEngineWizard_wtitle;
	public static String BrowserPart_showExternalTooltip;
	public static String BrowsersPreferencePage_message;
	public static String BrowsersPreferencePage_winfopop;
	public static String BrowsersPreferencePage_dinfopop;
	public static String BrowserPart_bookmarkTooltip;
	public static String BrowserPart_syncTocTooltip;	
	public static String BrowsersPreferencePage_wgroup;
	public static String BrowsersPreferencePage_view;
	public static String BrowsersPreferencePage_dgroup;
	public static String BrowsersPreferencePage_window;
	public static String InfoCenterPage_url;
	public static String InfoCenterPage_invalidURL;
	public static String InfoCenterPage_tocError;
	public static String WebSearchPage_label;
	public static String WebSearchPage_info;
	public static String LocalHelpPage_capabilityFiltering_name;
	public static String RootScopePage_masterButton;
	public static String RootScopePage_name;
	public static String RootScopePage_desc;
	public static String AllTopicsPart_collapseAll_tooltip;
	public static String AllTopicsPart_showAll_tooltip;
	public static String BookmarksPart_savedTopics;
	public static String BookmarksPart_delete;
	public static String BookmarksPart_deleteAll;
	public static String AskShowAll_dialogTitle;
	public static String AskShowAll_message;
	public static String AskShowAll_toggleMessage;
	
	public static String DefaultHelpUI_wtitle;
	public static String DefaultHelpUI_noPerspMessage;
	public static String ContextHelpDialog_showInDynamicHelp;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String BrowsersPreferencePage_openModeGroup;
	public static String BrowsersPreferencePage_openInPlace;
	public static String BrowsersPreferencePage_openInEditor;
	public static String ReusableHelpPart_internalBrowserTitle;
	public static String ReusableHelpPart_internalWebBrowserError;
	public static String ScopeSet_errorLoading;
	public static String ScopeSet_errorSaving;
}