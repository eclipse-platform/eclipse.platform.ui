/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.IViewPart;

import org.eclipse.search.ui.ISearchResultChangedListener;
import org.eclipse.search.ui.ISearchResultPresentation;
import org.eclipse.search.ui.SearchJobEvent;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.IPresentationFactory;
import org.eclipse.search.ui.text.ISearchElementPresentation;
import org.eclipse.search.ui.text.IStructureProvider;
import org.eclipse.search.ui.text.ITextSearchResult;
import org.eclipse.search.ui.text.Match;


/**
 * @author Thomas Mäder */
public class SearchResult implements ITextSearchResult {
	private Map fElementsToMatches;
	private List fListeners;
	private Object fDescription;
	private IPresentationFactory fPresentationFactory;
	private boolean fIsRunning= false;
	private IStructureProvider fStructureProvider;
	
	private static final Match[] EMPTY_ARRAY= new Match[0];
	
	public SearchResult(IStructureProvider structureProvider, IPresentationFactory factory, Object description) {
		super();
		fElementsToMatches= new HashMap();
		fListeners= new ArrayList();
		fPresentationFactory= factory;
		fDescription= description;
		fStructureProvider= structureProvider;
	}

	public Match[] getMatches(Object element) {
		synchronized (fElementsToMatches) {
			List matches= (List) fElementsToMatches.get(element);
			if (matches != null)
				return (Match[]) matches.toArray(new Match[matches.size()]);
			return EMPTY_ARRAY;
		}
	}

	public void addMatch(Match match) {
		synchronized (fElementsToMatches) {
			List matches= (List) fElementsToMatches.get(match.getElement());
			if (matches == null) {
				matches= new ArrayList();
				fElementsToMatches.put(match.getElement(), matches);
			}
			if (matches.contains(match))
				return;
			matches.add(match);
		}
		fireChange(MatchEvent.getSearchResultEvent(MatchEvent.ADDED, this, match));
	}

	public void removeAll() {
		synchronized (fElementsToMatches) {
			fElementsToMatches.clear();
		}
		fireChange(new RemoveAllEvent(this));
	}

	public void removeMatch(Match match) {
		boolean existed= false;
		synchronized (fElementsToMatches) {
			List matches= (List) fElementsToMatches.get(match.getElement());
			if (matches == null)
				return;
			existed= matches.remove(match);
			if (matches.isEmpty())
				fElementsToMatches.remove(match.getElement());
		}
		if (existed)
			fireChange(MatchEvent.getSearchResultEvent(MatchEvent.REMOVED, this, match));
	}

	public void addListener(ISearchResultChangedListener l) {
		synchronized (fListeners) {
			fListeners.add(l);
		}
	}

	public void removeListener(ISearchResultChangedListener l) {
		synchronized (fListeners) {
			fListeners.remove(l);
		}
	}
	void fireChange(SearchResultEvent e) {
		HashSet copiedListeners= new HashSet();
		synchronized (fListeners) {
			copiedListeners.addAll(fListeners);
		}
		Iterator listeners= copiedListeners.iterator();
		while (listeners.hasNext()) {
			((ISearchResultChangedListener) listeners.next()).searchResultsChanged(e);
		}
	}

	public Object getUserData() {
		return fDescription;
	}

	public ISearchResultPresentation createPresentation(IViewPart view) {
		return fPresentationFactory.createSearchResultPresentation(view);
	}
	
	public ISearchElementPresentation createElementPresentation(IViewPart view) {
		return fPresentationFactory.createElementPresentation(view);
	}
	
	public IStructureProvider getStructureProvider() {
		return fStructureProvider;
	}
	
	public int getMatchCount() {
		int count= 0;
		synchronized(fElementsToMatches) {
			for (Iterator elements= fElementsToMatches.values().iterator(); elements.hasNext();) {
				List element= (List) elements.next();
				if (element != null)
					count+= element.size();
			}
		}
		return count;
	}
	
	public int getMatchCount(Object element) {
		List matches= (List) fElementsToMatches.get(element);
		if (matches != null)
			return matches.size();
		return 0;
	}
	
	public void jobFinished() {
		fIsRunning= false;
		fireChange(new SearchJobEvent(this, SearchJobEvent.JOB_FINISHED));
	}

	public void jobStarted() {
		fIsRunning= true;
		fireChange(new SearchJobEvent(this, SearchJobEvent.JOB_STARTED));
	}
	
	public boolean isRunning() {
		return fIsRunning;
	}

	public Object[] getElements() {
		synchronized(fElementsToMatches) {
			return fElementsToMatches.keySet().toArray();
		}
	}

}
