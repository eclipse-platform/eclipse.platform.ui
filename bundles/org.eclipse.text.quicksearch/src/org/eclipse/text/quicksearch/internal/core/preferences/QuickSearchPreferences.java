/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.core.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.text.quicksearch.internal.core.QuickTextSearcher;
import org.eclipse.text.quicksearch.internal.core.priority.DefaultPriorityFunction;
import org.eclipse.text.quicksearch.internal.ui.QuickSearchActivator;
import org.eclipse.text.quicksearch.internal.util.LineReader;

/**
 * Helper class to access the QuickSearch Preferences.
 *
 * @author Kris De Volder
 */
public class QuickSearchPreferences {

	//Keys used to fetch 'raw' preferences values from the preferences store.
	public static final String IGNORED_EXTENSIONS = "ignored.extensions"; //$NON-NLS-1$
	public static final String IGNORED_NAMES = "ignored.names"; //$NON-NLS-1$
	public static final String IGNORED_PREFIXES = "ignored.prefixes"; //$NON-NLS-1$
	public static final String MAX_LINE_LEN = "LineReader.MAX_LINE_LEN"; //$NON-NLS-1$
	public static final String MAX_RESULTS = "QuickTextSearcher.MAX_RESULTS"; //$NON-NLS-1$
	private static boolean initializedDefaults;

	private IPreferenceStore store;

	public QuickSearchPreferences(IPreferenceStore preferenceStore) {
		this.store = preferenceStore;
		initializeDefaults();
	}

	public String[] getIgnoredExtensions() {
		return getAndParseStringList(IGNORED_EXTENSIONS);
	}

	public String[] getIgnoredPrefixes() {
		return getAndParseStringList(IGNORED_PREFIXES);
	}

	public String[] getIgnoredNames() {
		return getAndParseStringList(IGNORED_NAMES);
	}

	public int getMaxLineLen() {
		return store.getInt(MAX_LINE_LEN);
	}

	public int getMaxResults() {
		return store.getInt(MAX_RESULTS);
	}

	private String[] getAndParseStringList(String key) {
		String raw = store.getString(key);
		if (raw!=null) {
			return parseStringList(raw);
		}
		return null;
	}

	/**
	 * Takes a raw string list as entered in the prefs page input field and parses it.
	 * <p>
	 * Commas and newline are treated as 'separators' between elements. Further, any trailing
	 * and leading whitespace is stripped from individual elements and empty strings are silently
	 * dropped.
	 */
	private String[] parseStringList(String raw) {
		String[] elements = raw.split("[,\n]");  //$NON-NLS-1$
		List<String> list = new ArrayList<>(elements.length);
		for (String e : elements) {
			e = e.trim();
			if (!e.isEmpty()) {
				list.add(e);
			}
		}
		return list.toArray(new String[list.size()]);
	}

	public static void initializeDefaults() {
		if (!initializedDefaults) {
			initializedDefaults = true;
			IPreferenceStore store = QuickSearchActivator.getDefault().getPreferenceStore();
			store.setDefault(QuickSearchPreferences.MAX_LINE_LEN, LineReader.DEFAULT_MAX_LINE_LENGTH);
			store.setDefault(QuickSearchPreferences.MAX_RESULTS, QuickTextSearcher.DEFAULT_MAX_RESULTS);

			DefaultPriorityFunction dpf =  new DefaultPriorityFunction();
			store.setDefault(QuickSearchPreferences.IGNORED_EXTENSIONS, encode(dpf.ignoredExtensions));
			store.setDefault(QuickSearchPreferences.IGNORED_NAMES, encode(dpf.ignoredNames));
			store.setDefault(QuickSearchPreferences.IGNORED_PREFIXES, encode(dpf.ignoredPrefixes));
		}
	}

	private static String encode(String[] strings) {
		return String.join(", ", strings); //$NON-NLS-1$
	}


}
