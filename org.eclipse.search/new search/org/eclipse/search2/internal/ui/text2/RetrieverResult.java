/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IFile;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search.internal.ui.SearchPluginImages;

import org.eclipse.search2.internal.ui.SearchMessages;

/**
 * @author markus.schorn@windriver.com
 */
class RetrieverResult extends AbstractTextSearchResult implements ISearchResult, IEditorMatchAdapter, IFileMatchAdapter, IRetrieverKeys {
	private static final RetrieverMatch[] EMPTY_ARRAY= new RetrieverMatch[0];

	private Pattern fFilterExpr;
	private boolean fFilterHideMatching;
	private int fAcceptLocations= 0;
	private RetrieverQuery fQuery;
	private boolean fIsComplete;

	private HashMap fFilesToLines= new HashMap();
	private FilterMatchEvent fFilterEvent= new FilterMatchEvent(this);

	private RetrieverFilter fFilter;

	public RetrieverResult(RetrieverQuery query) {
		fQuery= query;
	}

	public void filter(RetrieverFilter filter) {
		fFilter= filter;
		boolean hideMatches= filter.getHideMatching();
		int acceptLocations= filter.getAcceptedLocations();
		Pattern filterPattern= filter.getPattern();
		// check if filter has changed at all
		if (fFilterHideMatching == hideMatches && acceptLocations == fAcceptLocations) {
			if (filterPattern == fFilterExpr || (filterPattern != null && filterPattern.equals(fFilterExpr))) {
				return;
			}
		}

		fFilterExpr= filterPattern;
		fFilterHideMatching= hideMatches;
		fAcceptLocations= acceptLocations;

		ArrayList changedMatches= new ArrayList();
		Object[] lines= getElements();
		for (int i= 0; i < lines.length; i++) {
			RetrieverLine line= (RetrieverLine) lines[i];
			line.filter(fFilterExpr, fFilterHideMatching, fAcceptLocations, changedMatches);
		}
		synchronized (fFilterEvent) {
			if (!changedMatches.isEmpty()) {
				fireChange(getFilterSearchResultEvent(changedMatches));
			}
		}
	}

	private SearchResultEvent getFilterSearchResultEvent(ArrayList matches) {
		Match[] matchArray= (Match[]) matches.toArray(new Match[matches.size()]);
		fFilterEvent.setMatches(matchArray);
		return fFilterEvent;
	}

	public boolean hasFilter() {
		return fFilterExpr != null || fAcceptLocations == ALL_LOCATIONS;
	}

	public IEditorMatchAdapter getEditorMatchAdapter() {
		return this;
	}

	public IFileMatchAdapter getFileMatchAdapter() {
		return this;
	}

	public String getLabel() {
		String pattern= fQuery.getSearchText();
		String scope= fQuery.getScopeDescription().getLabel();
		int matchCount= getMatchCount();

		if (pattern.length() == 0) {
			return SearchMessages.RetrieverResult_noInput_label;
		}
		return MessageFormat.format(SearchMessages.RetrieverResult_label, new Object[] {pattern, new Integer(matchCount), scope});
	}

	public String getTooltip() {
		return getLabel();
	}

	public ImageDescriptor getImageDescriptor() {
		return SearchPluginImages.DESC_OBJ_TSEARCH_DPDN;
	}

	public ISearchQuery getQuery() {
		return fQuery;
	}

	public Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file) {
		return collectMatches(getLinesForFile(file, false));
	}

	public RetrieverLine[] getLinesForFile(IFile file, boolean copy) {
		RetrieverLine[] lines= null;
		synchronized (fFilesToLines) {
			lines= (RetrieverLine[]) fFilesToLines.get(file);
		}
		return (copy && lines != null) ? (RetrieverLine[]) lines.clone() : lines;
	}

	public IFile getFile(Object element) {
		if (element instanceof IFile) {
			return (IFile) element;
		} else
			if (element instanceof RetrieverLine) {
				RetrieverLine line= (RetrieverLine) element;
				return line.getParent();
			}
		return null;
	}

	public boolean isShownInEditor(Match match, IEditorPart editor) {
		IEditorInput ei= editor.getEditorInput();
		if (ei instanceof IFileEditorInput) {
			IFileEditorInput fi= (IFileEditorInput) ei;
			Object line= match.getElement();
			if (line instanceof RetrieverLine) {
				return ((RetrieverLine) line).getParent().equals(fi.getFile());
			}
		}
		return false;
	}

	public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {
		IEditorInput ei= editor.getEditorInput();
		if (ei instanceof IFileEditorInput) {
			IFileEditorInput fi= (IFileEditorInput) ei;
			return computeContainedMatches(result, fi.getFile());
		}
		return EMPTY_ARRAY;
	}

	public void setLinesForFile(IFile file, List lines) {
		RetrieverLine[] newLines= (RetrieverLine[]) lines.toArray(new RetrieverLine[lines.size()]);
		RetrieverMatch[] oldMatches= collectMatches(getLinesForFile(file, false));
		synchronized (fFilesToLines) {
			fFilesToLines.remove(file);
		}
		if (oldMatches.length > 0) {
			removeMatches(oldMatches);
		}
		if (newLines.length > 0) {
			ArrayList matches= new ArrayList(newLines.length);
			for (Iterator iter= lines.iterator(); iter.hasNext();) {
				RetrieverLine line= (RetrieverLine) iter.next();
				line.filter(fFilterExpr, fFilterHideMatching, fAcceptLocations, null);
				RetrieverMatch[] m= line.getMatches(false);
				matches.addAll(Arrays.asList(m));
			}
			if (!matches.isEmpty()) {
				synchronized (fFilesToLines) {
					fFilesToLines.put(file, newLines);
				}
				addMatches((Match[]) matches.toArray(new Match[matches.size()]));
			}
		}
	}

	private RetrieverMatch[] collectMatches(RetrieverLine[] lines) {
		if (lines == null || lines.length == 0) {
			return EMPTY_ARRAY;
		}
		ArrayList matches= new ArrayList(lines.length);
		for (int i= 0; i < lines.length; i++) {
			RetrieverLine line= lines[i];
			matches.addAll(Arrays.asList(line.getMatches(false)));
		}
		return (RetrieverMatch[]) matches.toArray(new RetrieverMatch[matches.size()]);
	}

	public void removeAll() {
		synchronized (fFilesToLines) {
			fFilesToLines.clear();
		}
		super.removeAll();
	}

	public void removeMatch(Match match) {
		((RetrieverLine) match.getElement()).remove(Collections.singleton(match));
		super.removeMatch(match);
	}

	public void removeMatches(Match[] matches) {
		HashSet matchset= new HashSet();
		matchset.addAll(Arrays.asList(matches));
		HashSet lines= new HashSet();
		for (int i= 0; i < matches.length; i++) {
			RetrieverMatch match= (RetrieverMatch) matches[i];
			lines.add(match.getElement());
		}
		for (Iterator iter= lines.iterator(); iter.hasNext();) {
			RetrieverLine line= (RetrieverLine) iter.next();
			line.remove(matchset);
		}
		super.removeMatches(matches);
	}

	public int getMatchCount(Object element) {
		if (element instanceof RetrieverLine) {
			return ((RetrieverLine) element).getMatchCount();
		}
		return 0;
	}

	public int getDisplayedMatchCount(Object element) {
		if (element instanceof RetrieverLine) {
			return ((RetrieverLine) element).getDisplayedMatchCount();
		}
		return 0;
	}

	public boolean isComplete() {
		return fIsComplete;
	}

	public void setComplete(boolean isComplete) {
		fIsComplete= isComplete;
	}

	public int[] getDetailedMatchCount() {
		int[] result= new int[] {0, 0};
		synchronized (fFilesToLines) {
			for (Iterator iter= fFilesToLines.values().iterator(); iter.hasNext();) {
				RetrieverLine[] lines= (RetrieverLine[]) iter.next();
				for (int i= 0; i < lines.length; i++) {
					RetrieverLine line= lines[i];
					line.addMatchCount(result);
				}
			}
		}
		return result;
	}

	public void searchAgain(Collection outdated) {
		if (fQuery != null) {
			fQuery.searchAgain(outdated, new NullProgressMonitor());
		}
	}

	public RetrieverFilter getFilter() {
		return fFilter;
	}
}
