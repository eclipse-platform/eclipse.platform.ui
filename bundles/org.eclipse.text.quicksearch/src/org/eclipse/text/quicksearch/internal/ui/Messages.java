/*******************************************************************************
 * Copyright (c) 2019, 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.text.quicksearch.internal.ui.messages"; //$NON-NLS-1$
	public static String QuickSearchPreferencesPage_Tooltip_Extensions;
	public static String QuickSearchPreferencesPage_Tooltip_Prefixes;
	public static String QuickSearchPreferencesPage_Tooltip_Names;
	public static String QuickSearchPreferencesPage_MaxLineLength;
	public static String QuickSearchPreferencesPage_Tooltip_MaxLineLength;
	public static String QuickSearchPreferencesPage_Tooltip_MaxResults;
	public static String QuickSearchPreferencesPage_MaxResults;
	public static String QuickSearchPreferencesPage_Ignored_Extensions;
	public static String QuickSearchPreferencesPage_Ignored_Prefixes;
	public static String QuickSearchPreferencesPage_Ignored_Names;
	public static String QuickSearchDialog_Open;
	public static String QuickSearchDialog_Refresh;
	public static String QuickSearchDialog_In;
	public static String QuickSearchDialog_InTooltip;
	public static String QuickSearchDialog_line;
	public static String QuickSearchDialog_text;
	public static String QuickSearchDialog_path;
	public static String QuickSearchDialog_RefreshJob;
	public static String QuickSearchDialog_searching;
	public static String QuickSearchDialog_keepOpen_toggle;
	public static String QuickSearchDialog_caseSensitive_toggle;
	public static String QuickSearchDialog_title;
	public static String QuickSearchDialog_listLabel;
	public static String QuickSearchDialog_listLabel_limit_reached;
	public static String QuickSearchDialog_caseSensitive_label;
	public static String QuickSearchDialog_caseInsensitive_label;
	public static String QuickTextSearch_updateMatchesJob;
	public static String quickAccessMatch;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
