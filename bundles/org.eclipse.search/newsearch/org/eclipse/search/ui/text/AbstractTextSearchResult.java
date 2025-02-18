/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.search.ui.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;

/**
 * An abstract base implementation for text-match based search results. This search
 * result implementation consists of a list of {@link org.eclipse.search.ui.text.Match matches}.
 * No assumptions are made about the kind of elements these matches are reported against.
 *
 * @since 3.0
 */
public abstract class AbstractTextSearchResult implements ISearchResult {

	private static final Match[] EMPTY_ARRAY= new Match[0];

	private final ConcurrentMap<Object, Set<Match>> fElementsToMatches;
	private final List<ISearchResultListener> fListeners;
	private final AtomicInteger matchCount;

	private MatchFilter[] fMatchFilters;

	/**
	 * Constructs a new <code>AbstractTextSearchResult</code>
	 */
	protected AbstractTextSearchResult() {
		fElementsToMatches= new ConcurrentHashMap<>();
		fListeners = new CopyOnWriteArrayList<>();
		matchCount = new AtomicInteger(0);
		fMatchFilters= null; // filtering disabled by default
	}

	/**
	 * Returns an array with all matches reported against the given element.
	 * Note that all matches of the given element are returned. The filter state
	 * of the matches is not relevant. The matches are reported sorted per
	 * offset and length. The order may be important for example stepping
	 * between matches (see bug 58417). For calculating just a match count the
	 * order is not needed and the faster {@link #getMatchSet(Object)} should be used instead.
	 * The order of reported matches found (with equal offset and length) is not preserved
	 * (Does not make sense	during parallel search).
	 *
	 * @param element
	 *            the element to report matches for
	 * @return all matches reported for this element
	 * @see Match#getElement()
	 */
	public Match[] getMatches(Object element) {
		if (element == null) {
			return EMPTY_ARRAY;
		}
		Set<Match> matches = fElementsToMatches.get(element);
		if (matches != null) {
			Match[] sortingCopy = matches.toArray(new Match[matches.size()]);
			Arrays.sort(sortingCopy, AbstractTextSearchResult::compare);
			return sortingCopy;
		}
		return EMPTY_ARRAY;
	}

	/**
	 * Returns an Enumeration of all matches reported against the given element.
	 * Note that all matches of the given element are returned. The filter state
	 * of the matches is not relevant. Like {@link #getMatches(Object)} but
	 * unordered result.
	 *
	 * @param element
	 *            the element to report matches for
	 * @return all matches reported for this element
	 * @since 3.14
	 * @see AbstractTextSearchResult#getMatches(Object)
	 */
	public Enumeration<Match> getMatchSet(Object element) {
		if (element == null) {
			return Collections.emptyEnumeration();
		}
		Set<Match> matches = fElementsToMatches.get(element);
		if (matches != null) {
			return Collections.enumeration(matches);
		}
		return Collections.emptyEnumeration();
	}

	/**
	 * Adds a <code>Match</code> to this search result. This method does nothing if the
	 * match is already present.
	 * <p>
	 * Subclasses may extend this method.
	 * </p>
	 *
	 * @param match the match to add
	 */
	public void addMatch(Match match) {
		if (didAddMatch(match))
			fireChange(getSearchResultEvent(match, MatchEvent.ADDED));
	}

	/**
	 * Adds a number of Matches to this search result. This method does nothing for
	 * matches that are already present.
	 * <p>
	 * Subclasses may extend this method.
	 * </p>
	 * @param matches the matches to add
	 */
	public void addMatches(Match[] matches) {
		Collection<Match> reallyAdded= new ArrayList<>();
		for (Match match : matches) {
			if (didAddMatch(match)) {
				reallyAdded.add(match);
			}
		}
		if (!reallyAdded.isEmpty())
			fireChange(getSearchResultEvent(reallyAdded, MatchEvent.ADDED));
	}

	private MatchEvent getSearchResultEvent(Match match, int eventKind) {
		MatchEvent fMatchEvent = new MatchEvent(this);
		fMatchEvent.setKind(eventKind);
		fMatchEvent.setMatch(match);
		return fMatchEvent;
	}

	private MatchEvent getSearchResultEvent(Collection<Match> matches, int eventKind) {
		MatchEvent fMatchEvent = new MatchEvent(this);
		fMatchEvent.setKind(eventKind);
		Match[] matchArray= matches.toArray(new Match[matches.size()]);
		fMatchEvent.setMatches(matchArray);
		return fMatchEvent;
	}

	private boolean didAddMatch(Match match) {
		matchCount.set(0);
		updateFilterState(match);
		return fElementsToMatches.computeIfAbsent(match.getElement(), k -> ConcurrentHashMap.newKeySet()).add(match);
	}

	private static int compare(Match match2, Match match1) {
		int diff= match2.getOffset()-match1.getOffset();
		if (diff != 0)
			return diff;
		return match2.getLength()-match1.getLength();
	}

	/**
	 * Removes all matches from this search result.
	 * <p>
	 * Subclasses may extend this method.
	 * </p>
	 */
	public void removeAll() {
		doRemoveAll();
		fireChange(new RemoveAllEvent(this));
	}
	private void doRemoveAll() {
		matchCount.set(0);
		fElementsToMatches.clear();
	}

	/**
	 * Removes the given match from this search result. This method has no
	 * effect if the match is not found.
	 * <p>
	 * Subclasses may extend this method.
	 * </p>
	 * @param match the match to remove
	 */
	public void removeMatch(Match match) {
		if (didRemoveMatch(match))
			fireChange(getSearchResultEvent(match, MatchEvent.REMOVED));
	}

	/**
	 * Removes the given matches from this search result. This method has no
	 * effect for matches that are not found
	 * <p>
	 * Subclasses may extend this method.
	 * </p>
	 *
	 * @param matches the matches to remove
	 */
	public void removeMatches(Match[] matches) {
		Collection<Match> existing= new ArrayList<>();
		for (Match match : matches) {
			if (didRemoveMatch(match))
				existing.add(match); // no duplicate matches at this point
		}
		if (!existing.isEmpty())
			fireChange(getSearchResultEvent(existing, MatchEvent.REMOVED));
	}


	private boolean didRemoveMatch(Match match) {
		matchCount.set(0);
		boolean[] existed = new boolean[1];
		fElementsToMatches.computeIfPresent(match.getElement(), (f, matches) -> {
			existed[0] = matches.remove(match);
			if (matches.isEmpty()) {
				return null; // remove
			}
			return matches;
		});
		return existed[0];
	}

	@Override
	public void addListener(ISearchResultListener l) {
		fListeners.add(l);
	}

	@Override
	public void removeListener(ISearchResultListener l) {
		fListeners.remove(l);
	}

	/**
	 * Send the given <code>SearchResultEvent</code> to all registered search
	 * result listeners.
	 *
	 * @param e the event to be sent
	 *
	 * @see ISearchResultListener
	 */
	protected void fireChange(SearchResultEvent e) {
		for (ISearchResultListener listener : fListeners) {
			listener.searchResultChanged(e);
		}
	}

	private void updateFilterStateForAllMatches() {
		boolean disableFiltering= getActiveMatchFilters() == null;
		ArrayList<Match> changed= new ArrayList<>();
		Object[] elements= getElements();
		for (Object element : elements) {
			Match[] matches= getMatches(element);
			for (Match match : matches) {
				if (disableFiltering || updateFilterState(match)) {
					changed.add(match);
				}
			}
		}
		Match[] allChanges = changed.toArray(Match[]::new);
		fireChange(new FilterUpdateEvent(this, allChanges, getActiveMatchFilters()));
	}

	/*
	 * Evaluates the filter for the match and updates it. Return true if the filter changed.
	 */
	private boolean updateFilterState(Match match) {
		MatchFilter[] matchFilters= getActiveMatchFilters();
		if (matchFilters == null) {
			return false; // do nothing, no change
		}

		boolean oldState= match.isFiltered();
		for (MatchFilter matchFilter : matchFilters) {
			if (matchFilter.filters(match)) {
				match.setFiltered(true);
				return !oldState;
			}
		}
		match.setFiltered(false);
		return oldState;
	}

	/**
	 * Returns the total number of matches contained in this search result.
	 * The filter state of the matches is not relevant when counting matches. All matches are counted.
	 *
	 * @return total number of matches
	 */
	public int getMatchCount() {
		final int oldCount = matchCount.get();
		if (oldCount != 0) {
			return oldCount;
		}
		// The oldCount is zero here => we have to calculate again
		int newCount = 0;
		for (Set<Match> element : fElementsToMatches.values()) {
			newCount += element.size();
		}
		if (matchCount.compareAndSet(0, newCount)) {
			// Only return if not changed meanwhile
			return newCount;
		}
		// Changed once again, fetch again latest value
		return getMatchCount();
	}

	/**
	 * @return {@code true} if the result is not empty
	 * @since 3.17
	 */
	public boolean hasMatches() {
		for (Entry<Object, Set<Match>> entry : fElementsToMatches.entrySet()) {
			if (!entry.getValue().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the number of matches reported against a given element. This is
	 * equivalent to calling <code>getMatches(element).length</code>
	 * The filter state of the matches is not relevant when counting matches. All matches are counted.
	 *
	 * @param element the element to get the match count for
	 * @return the number of matches reported against the element
	 */
	public int getMatchCount(Object element) {
		if (element == null) {
			return 0;
		}
		Set<Match> matches = fElementsToMatches.get(element);
		if (matches != null)
			return matches.size();
		return 0;
	}

	/**
	 * Returns an array containing the set of all elements that matches are
	 * reported against in this search result.
	 * Note that all elements that contain matches are returned. The filter state of the matches is not relevant.
	 *
	 * @return the set of elements in this search result
	 */
	public Object[] getElements() {
		return fElementsToMatches.keySet().toArray();
	}

	/**
	 * Returns a number of all elements that have matches. Note that count of
	 * all elements that contain matches are returned. The filter state of the
	 * matches is not relevant.
	 *
	 * @return the number of elements in this search result
	 * @since 3.17
	 */
	public int getElementsCount() {
		return fElementsToMatches.size();
	}

	/**
	 * Sets the active match filters for this result. If set to non-null, the match filters will be used to update the filter
	 * state ({@link Match#isFiltered()} of matches and the {@link AbstractTextSearchViewPage} will only
	 * show non-filtered matches. If <code>null</code> is set
	 * the filter state of the match is ignored by the {@link AbstractTextSearchViewPage} and all matches
	 * are shown.
	 * Note the model contains all matches, regardless if the filter state of a match.
	 *
	 * @param filters the match filters to set or <code>null</code> if the filter state of the match
	 * should be ignored.
	 *
	 * @since 3.3
	 */
	public void setActiveMatchFilters(MatchFilter[] filters) {
		fMatchFilters= filters;
		updateFilterStateForAllMatches();
	}

	/**
	 * Returns the active match filters for this result. If not null is returned, the match filters will be used to update the filter
	 * state ({@link Match#isFiltered()} of matches and the {@link AbstractTextSearchViewPage} will only
	 * show non-filtered matches. If <code>null</code> is set
	 * the filter state of the match is ignored by the {@link AbstractTextSearchViewPage} and all matches
	 * are shown.
	 *
	 * @return the match filters to be used or <code>null</code> if the filter state of the match
	 * should be ignored.
	 *
	 * @since 3.3
	 */
	public MatchFilter[] getActiveMatchFilters() {
		return fMatchFilters;
	}

	/**
	 * Returns all applicable filters for this result or null if match filters are not supported. If match filters are returned,
	 * the {@link AbstractTextSearchViewPage} will contain menu entries in the view menu.
	 *
	 * @return all applicable filters for this result.
	 *
	 * @since 3.3
	 */
	public MatchFilter[] getAllMatchFilters() {
		return null;
	}


	/**
	 * Returns an implementation of <code>IEditorMatchAdapter</code> appropriate
	 * for this search result.
	 *
	 * @return an appropriate adapter or <code>null</code> if none has been implemented
	 *
	 * @see IEditorMatchAdapter
	 */
	public abstract IEditorMatchAdapter getEditorMatchAdapter();


	/**
	 * Returns an implementation of <code>IFileMatchAdapter</code> appropriate
	 * for this search result.
	 *
	 * @return an appropriate adapter or <code>null</code> if none has been implemented
	 *
	 * @see IFileMatchAdapter
	 */
	public abstract IFileMatchAdapter getFileMatchAdapter();
}
