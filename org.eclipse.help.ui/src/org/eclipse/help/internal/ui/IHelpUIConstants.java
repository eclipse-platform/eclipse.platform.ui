package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
/**
 * Interface for holding UI constants
 */
public interface IHelpUIConstants {
	// Help UI pluging id with a "." for convenience.
	public static final String HELP_UI_PLUGIN_ID = "org.eclipse.help.ui.";
	// F1 ids
	public static final String F1_SHELL = HELP_UI_PLUGIN_ID + "f1Shell";
	public static final String PREF_PAGE_BROWSERS = HELP_UI_PLUGIN_ID + "prefPageBrowsers";
	public static final String SEARCH_FILTERING_OPTIONS = HELP_UI_PLUGIN_ID + "searchFilteringOpts";
	public static final String SEARCH_PAGE = HELP_UI_PLUGIN_ID + "searchPage";
	public static final String HIT_MARKER_ID = HELP_UI_PLUGIN_ID + "helpsearchhit";
	public static final String HIT_MARKER_ATTR_HREF = "href";
	public static final String HIT_MARKER_ATTR_LABEL = "label";
	public static final String HIT_MARKER_ATTR_ORDER = "order";
	public static final String HIT_MARKER_ATTR_RESULTOF = "resultfof";
	public static final String RESULTS_PAGE_ID = HELP_UI_PLUGIN_ID + "searchPage";
	public static final String IMAGE_KEY_SEARCH = "search_icon";
	public static final String IMAGE_KEY_TOPIC = "s_topic_icon";
}