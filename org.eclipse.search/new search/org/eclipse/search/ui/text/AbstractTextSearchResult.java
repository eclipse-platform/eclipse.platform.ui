/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.search.ui.text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.ui.IEditorPart;
public abstract class AbstractTextSearchResult implements ISearchResult {
	private Map fElementsToMatches;
	private List fListeners;
	private static final Match[] EMPTY_ARRAY= new Match[0];
	private MatchEvent fMatchEvent;

	/**
	 * Constructor
	 */
	protected AbstractTextSearchResult() {
		fElementsToMatches= new HashMap();
		fListeners= new ArrayList();
		fMatchEvent= new MatchEvent(this);
	}

	/**
	 * Returns an array with all matches reported against the given element.
	 * 
	 * @see Match#getElement()
	 * @param element The element to report matches for.
	 * @return All matches reported for this element.
	 */
	public Match[] getMatches(Object element) {
		synchronized (fElementsToMatches) {
			return doGetMatches(element);
		}
	}
	private Match[] doGetMatches(Object element) {
		List matches= (List) fElementsToMatches.get(element);
		if (matches != null)
			return (Match[]) matches.toArray(new Match[matches.size()]);
		return EMPTY_ARRAY;
	}
	/**
	 * Adds a Match to this search result. This method does nothing if if the
	 * Match is already present.
	 * 
	 * @param match The match to add.
	 */
	public void addMatch(Match match) {
		boolean hasAdded= false;
		synchronized (fElementsToMatches) {
			hasAdded= doAddMatch(match);
		}
		if (hasAdded)
			fireChange(getSearchResultEvent(match, MatchEvent.ADDED));
	}
	
	public void addMatches(Match[] matches) {

		Set reallyAdded= new HashSet();
		for (int i = 0; i < matches.length; i++) {
			synchronized (fElementsToMatches) {
				if (doAddMatch(matches[i]))
					reallyAdded.add(matches[i]);
			}
		}
		if (reallyAdded.size() > 0)
			fireChange(getSearchResultEvent(reallyAdded, MatchEvent.ADDED));
	}
	
	private SearchResultEvent getSearchResultEvent(Match match, int eventKind) {
		fMatchEvent.setKind(eventKind);
		fMatchEvent.setMatch(match);
		return fMatchEvent;
	}
		


	private MatchEvent getSearchResultEvent(Set matches, int eventKind) {
		fMatchEvent.setKind(eventKind);
		Match[] matchArray= new Match[matches.size()];
		matches.toArray(matchArray);
		fMatchEvent.setMatches(matchArray);
		return fMatchEvent;
	}

	private boolean doAddMatch(Match match) {
		List matches= (List) fElementsToMatches.get(match.getElement());
		if (matches == null) {
			matches= new ArrayList();
			fElementsToMatches.put(match.getElement(), matches);
		}
		if (!matches.contains(match)) {
			matches.add(match);
			return true;
		}
		return false;
	}
	/**
	 * Removes all matches from this search result.
	 */
	public void removeAll() {
		synchronized (fElementsToMatches) {
			doRemoveAll();
		}
		fireChange(new RemoveAllEvent(this));
	}
	private void doRemoveAll() {
		fElementsToMatches.clear();
	}
	/**
	 * Removes the given match from this search result. This method has no
	 * effect if the match is not found.
	 * 
	 * @param match The match to remove.
	 */
	public void removeMatch(Match match) {
		boolean existed= false;
		synchronized (fElementsToMatches) {
			existed= doRemoveMatch(match);
		}
		if (existed)
			fireChange(getSearchResultEvent(match, MatchEvent.REMOVED));
	}
	
	public void removeMatches(Match[] matches) {
		Set existing= new HashSet();
		for (int i = 0; i < matches.length; i++) {
			synchronized (fElementsToMatches) {
				if (doRemoveMatch(matches[i]))
					existing.add(matches[i]);
			}
		}
		if (existing.size() > 0)
			fireChange(getSearchResultEvent(existing, MatchEvent.REMOVED));
	}

	
	private boolean doRemoveMatch(Match match) {
		boolean existed= false;
		List matches= (List) fElementsToMatches.get(match.getElement());
		if (matches != null) {
			existed= matches.remove(match);
			if (matches.isEmpty())
				fElementsToMatches.remove(match.getElement());
		}
		return existed;
	}
	/**
	 * {@inheritDoc}
	 */
	public void addListener(ISearchResultListener l) {
		synchronized (fListeners) {
			fListeners.add(l);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	public void removeListener(ISearchResultListener l) {
		synchronized (fListeners) {
			fListeners.remove(l);
		}
	}
	
	/**
	 * Send the given <code>SearchResultEvent<code> to all registered search
	 * result listeners
	 * @see ISearchResultListener
	 * @param e The event to be sent.
	 */
	protected void fireChange(SearchResultEvent e) {
		HashSet copiedListeners= new HashSet();
		synchronized (fListeners) {
			copiedListeners.addAll(fListeners);
		}
		Iterator listeners= copiedListeners.iterator();
		while (listeners.hasNext()) {
			((ISearchResultListener) listeners.next()).searchResultChanged(e);
		}
	}
	/**
	 * Returns the total number of matches contained in this search result.
	 * 
	 * @return Total number of matches.
	 */
	public int getMatchCount() {
		int count= 0;
		synchronized (fElementsToMatches) {
			for (Iterator elements= fElementsToMatches.values().iterator(); elements.hasNext();) {
				List element= (List) elements.next();
				if (element != null)
					count+= element.size();
			}
		}
		return count;
	}
	/**
	 * Returns the number of matches reported against a given element. This is
	 * equivalent to calling <code>getMatches(element).length</code>
	 * 
	 * @param element The element to get the match count for.
	 * @return The number of matches reported against the element.
	 */
	public int getMatchCount(Object element) {
		List matches= (List) fElementsToMatches.get(element);
		if (matches != null)
			return matches.size();
		return 0;
	}
	/**
	 * Returns an array containing the set of all elements that matches are
	 * reported against in this search result.
	 * 
	 * @return The set of elements in this search result. 
	 */
	public Object[] getElements() {
		synchronized (fElementsToMatches) {
			return fElementsToMatches.keySet().toArray();
		}
	}
	
		
	/**
	 * Returns an array with all matches contained in the given file. If the
	 * matches are not contained within an <code>IFile</code>, this method
	 * must return an empty array.
	 * 
	 * @param file The file to find matches in.
	 * @return An array of matches (possibly empty).
	 */
	public abstract Match[] findContainedMatches(IFile file);
	/**
	 * Returns the file associated with the given element (usually the file
	 * the element is contained in). If the element is not associated with a
	 * file, this method should return <code>null</code>.
	 * 
	 * @param element An element associated with a match.
	 * @return The file associated with the element or null.
	 */
	public abstract IFile getFile(Object element);
	/**
	 * Determines whether a match should be displayed in the given editor.
	 * For example, if a match is reported in a file, This method should return 
	 * <code>true</code>, if the given editor displays the file. 
	 * 
	 * @param match The match.
	 * @param editor The editor that possibly contains the matches element.
	 * @return
	 */
	public abstract boolean isShownInEditor(Match match, IEditorPart editor);
	/**
	 * Returns all matches that are contained in the element shown in the given
	 * editor.
	 * For example, if the editor shows a particular file, all matches in that file should
	 * be returned.
	 * 
	 * @param editor The editor.
	 * @return All matches that are contained in the element that is shown in
	 *         the given editor.
	 */
	public abstract Match[] findContainedMatches(IEditorPart editor);
	

}
