/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *     Red Hat Inc. - add support for filtering files from innermost nested projects
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.util.HashSet;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchFilter;

public class FileSearchResult extends AbstractTextSearchResult implements IEditorMatchAdapter, IFileMatchAdapter {
	private final Match[] EMPTY_ARR= new Match[0];

	private FileSearchQuery fQuery;

	public FileSearchResult(FileSearchQuery job) {
		fQuery= job;
		setActiveMatchFilters(getLastUsedFilters());
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return SearchPluginImages.DESC_OBJ_TSEARCH_DPDN;
	}

	@Override
	public String getLabel() {
		return fQuery.getResultLabel(getMatchCount());
	}

	@Override
	public String getTooltip() {
		return getLabel();
	}

	private static MatchFilter INNERMOST_PROJECT = new OuterProjectFileFilter();
	private static MatchFilter[] ALL_MATCH_FILTERS = new MatchFilter[] { INNERMOST_PROJECT };
	private static final String SETTINGS_LAST_USED_FILTERS = "filters_last_used"; //$NON-NLS-1$

	@Override
	public MatchFilter[] getAllMatchFilters() {
		return ALL_MATCH_FILTERS;
	}

	@Override
	public synchronized void setActiveMatchFilters(MatchFilter[] filters) {
		// TODO Auto-generated method stub
		super.setActiveMatchFilters(filters);
		setLastUsedFilters(filters);
	}

	@Override
	public synchronized MatchFilter[] getActiveMatchFilters() {
		// TODO Auto-generated method stub
		return super.getActiveMatchFilters();
	}

	public static MatchFilter[] getLastUsedFilters() {
		String string = SearchPlugin.getDefault().getDialogSettings().get(SETTINGS_LAST_USED_FILTERS);
		if (string != null) {
			return decodeFiltersString(string);
		}
		return new MatchFilter[0];
	}

	public static void setLastUsedFilters(MatchFilter[] filters) {
		String encoded = encodeFilters(filters);
		SearchPlugin.getDefault().getDialogSettings().put(SETTINGS_LAST_USED_FILTERS, encoded);
	}

	private static String encodeFilters(MatchFilter[] enabledFilters) {
		StringBuilder buf = new StringBuilder();
		for (MatchFilter matchFilter : enabledFilters) {
			buf.append(matchFilter.getID());
			buf.append(';');
		}
		return buf.toString();
	}

	private static MatchFilter[] decodeFiltersString(String encodedString) {
		StringTokenizer tokenizer = new StringTokenizer(encodedString, String.valueOf(';'));
		HashSet<MatchFilter> result = new HashSet<>();
		while (tokenizer.hasMoreTokens()) {
			MatchFilter curr = findMatchFilter(tokenizer.nextToken());
			if (curr != null) {
				result.add(curr);
			}
		}
		return result.toArray(new MatchFilter[result.size()]);
	}

	private static MatchFilter findMatchFilter(String id) {
		for (MatchFilter matchFilter : ALL_MATCH_FILTERS) {
			if (matchFilter.getID().equals(id))
				return matchFilter;
		}
		return null;
	}

	@Override
	public Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file) {
		return getMatches(file);
	}

	@Override
	public IFile getFile(Object element) {
		if (element instanceof IFile)
			return (IFile)element;
		return null;
	}

	@Override
	public boolean isShownInEditor(Match match, IEditorPart editor) {
		IEditorInput ei= editor.getEditorInput();
		if (ei instanceof IFileEditorInput) {
			IFileEditorInput fi= (IFileEditorInput) ei;
			return match.getElement().equals(fi.getFile());
		}
		return false;
	}

	@Override
	public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {
		IEditorInput ei= editor.getEditorInput();
		if (ei instanceof IFileEditorInput) {
			IFileEditorInput fi= (IFileEditorInput) ei;
			return getMatches(fi.getFile());
		}
		return EMPTY_ARR;
	}

	@Override
	public ISearchQuery getQuery() {
		return fQuery;
	}

	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		return this;
	}

	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return this;
	}
}
