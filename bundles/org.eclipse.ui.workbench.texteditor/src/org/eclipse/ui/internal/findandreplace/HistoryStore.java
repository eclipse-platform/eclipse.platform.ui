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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogSettings;

/**
 * Store for search histories or replace histories inside of a dialog. Stores
 * the nodes using the DialogSettings mechanism.
 */
public class HistoryStore {
	public static final String SEARCH_HISTORY_KEY = "searchhistory"; //$NON-NLS-1$
	public static final String REPLACE_HISTORY_KEY = "replacehistory"; //$NON-NLS-1$

	private IDialogSettings settingsManager;
	private int historySize;
	private List<String> history;
	private String sectionName;

	/**
	 * @param settingsManager manager for DialogSettings
	 * @param sectionName     the name of the section in the DialogSettings
	 *                        containing the history
	 * @param historySize     how many entries to keep in the history
	 */
	public HistoryStore(IDialogSettings settingsManager, String sectionName, int historySize) {
		this.settingsManager = settingsManager;
		this.historySize = historySize;
		loadSection(sectionName);
	}

	public Iterable<String> get() {
		loadSection(sectionName);
		return history;
	}

	public String get(int index) {
		loadSection(sectionName);
		return history.get(index);
	}


	public void add(String historyItem) {
		if (sectionName == null) {
			throw new IllegalStateException("No section loaded"); //$NON-NLS-1$
		}
		if (historyItem != null && !historyItem.isEmpty()) {
			history.add(0, historyItem);
		}

		writeHistory();
	}

	public void remove(String historyItem) {
		int indexInHistory = history.indexOf(historyItem);
		if (indexInHistory >= 0) {
			history.remove(indexInHistory);
		}

		writeHistory();
	}

	public void addOrPushToTop(String historyItem) {
		remove(historyItem);
		add(historyItem);
	}

	public boolean isEmpty() {
		loadSection(sectionName);
		return history.isEmpty();
	}

	private void loadSection(String newSectionName) {
		this.sectionName = newSectionName;
		history = new ArrayList<>();

		String[] newHistoryEntries = settingsManager.getArray(newSectionName);
		if (newHistoryEntries != null) {
			history.addAll(Arrays.asList(newHistoryEntries));
		}
	}

	/**
	 * Writes the given history into the given dialog store.
	 */
	private void writeHistory() {
		int itemCount = history.size();
		Set<String> distinctItems = new HashSet<>(itemCount);
		for (int i = 0; i < itemCount; i++) {
			String item = history.get(i);
			if (distinctItems.contains(item)) {
				history.remove(i--);
				itemCount--;
			} else {
				distinctItems.add(item);
			}
		}

		while (history.size() > historySize) {
			history.remove(historySize);
		}

		String[] names = new String[history.size()];
		history.toArray(names);
		settingsManager.put(sectionName, names);
	}

	public int indexOf(String entry) {
		loadSection(sectionName);
		return history.indexOf(entry);
	}

	public int size() {
		loadSection(sectionName);
		return history.size();
	}
}
