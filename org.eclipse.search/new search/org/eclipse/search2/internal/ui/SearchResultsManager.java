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
package org.eclipse.search2.internal.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultManagerListener;
import org.eclipse.search.ui.ISearchResultManager;

public class SearchResultsManager implements ISearchResultManager {
	private List fSearches;
	private List fListeners;
	public SearchResultsManager() {
		super();
		// an ArrayList should be plenty fast enough (few searches).
		fSearches= new ArrayList();
		fListeners= new ArrayList();
	}
	public synchronized ISearchResult[] getSearchResults() {
		ISearchResult[] result= new ISearchResult[fSearches.size()];
		return (ISearchResult[]) fSearches.toArray(result);
	}

	public void removeSearchResult(ISearchResult search) {
		synchronized (fSearches) {
			fSearches.remove(search);
		}
		fireRemoved(search);
	}

	public void addSearchResult(ISearchResult search) {
		synchronized (fSearches) {
			if (fSearches.contains(search))
				return;
			fSearches.add(search);
		}
		fireAdded(search);
	}
	
	public void addSearchResultListener(ISearchResultManagerListener l) {
		synchronized (fListeners) {
			fListeners.add(l);
		}
	}

	public void removeSearchResultListener(ISearchResultManagerListener l) {
		synchronized (fListeners) {
			fListeners.remove(l);
		}
	}
	void fireAdded(ISearchResult search) {
		Set copiedListeners= new HashSet();
		synchronized (fListeners) {
			copiedListeners.addAll(fListeners);
		}
		Iterator listeners= copiedListeners.iterator();
		while (listeners.hasNext()) {
			ISearchResultManagerListener l= (ISearchResultManagerListener) listeners.next();
			l.searchResultAdded(search);
		}
	}

	void fireRemoved(ISearchResult search) {
		Set copiedListeners= new HashSet();
		synchronized (fListeners) {
			copiedListeners.addAll(fListeners);
		}
		Iterator listeners= copiedListeners.iterator();
		while (listeners.hasNext()) {
			ISearchResultManagerListener l= (ISearchResultManagerListener) listeners.next();
			l.searchResultRemoved(search);
		}
	}

	public void removeAll() {
		Set copiedSearches= new HashSet();
		synchronized (fSearches) {
			copiedSearches.addAll(fSearches);
			fSearches.clear();
			Iterator iter= copiedSearches.iterator();
			while (iter.hasNext()) {
				ISearchResult element= (ISearchResult) iter.next();
				fireRemoved(element);
			}
		}
	}

}
