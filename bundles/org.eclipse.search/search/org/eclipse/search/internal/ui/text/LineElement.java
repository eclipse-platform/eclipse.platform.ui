/*******************************************************************************
 * Copyright (c) 2007, 2026 IBM Corporation and others.
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

package org.eclipse.search.internal.ui.text;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.resources.IResource;

import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

/**
 * Element representing a line in a file
 */
public class LineElement {

	private final IResource fParent;

	private final int fLineNumber;
	private final int fLineStartOffset;
	private final String fLineContents;

	public LineElement(IResource parent, int lineNumber, int lineStartOffset, String lineContents) {
		fParent= parent;
		fLineNumber= lineNumber;
		fLineStartOffset= lineStartOffset;
		fLineContents= lineContents;
	}

	public IResource getParent() {
		return fParent;
	}

	public int getLine() {
		return fLineNumber;
	}

	public String getContents() {
		return fLineContents;
	}

	public int getOffset() {
		return fLineStartOffset;
	}

	public boolean contains(int offset) {
		return fLineStartOffset <= offset && offset < fLineStartOffset + fLineContents.length();
	}

	public int getLength() {
		return fLineContents.length();
	}

	/**
	 * Returns all matches of this line, no matter if they are shown in the viewer
	 * or hidden by an active match filter.
	 *
	 * @param result the search result the matches belong to
	 * @return all matches of this line
	 */
	public FileMatch[] getMatches(AbstractTextSearchResult result) {
		return toArray(collectMatches(result, false, Integer.MAX_VALUE));
	}

	/**
	 * Returns the matches of this line that are shown in the viewer: matches hidden
	 * by an active match filter
	 * ({@link AbstractTextSearchResult#getActiveMatchFilters()}) are not returned.
	 *
	 * @param result the search result the matches belong to
	 * @return the matches of this line that are not hidden by a match filter
	 */
	public FileMatch[] getDisplayedMatches(AbstractTextSearchResult result) {
		return toArray(collectMatches(result, true, Integer.MAX_VALUE));
	}

	/**
	 * Counts the matches of this line that are shown in the viewer: matches hidden
	 * by an active match filter
	 * ({@link AbstractTextSearchResult#getActiveMatchFilters()}) are not counted.
	 *
	 * @param result the search result the matches belong to
	 * @return number of matches of this line that are not hidden by a match filter
	 */
	public int getNumberOfDisplayedMatches(AbstractTextSearchResult result) {
		return collectMatches(result, true, Integer.MAX_VALUE).size();
	}

	/**
	 * @param result the search result the matches belong to
	 * @return <code>true</code> if this line has at least one match that is not
	 *         hidden by an active match filter
	 */
	public boolean hasDisplayedMatches(AbstractTextSearchResult result) {
		return !collectMatches(result, true, 1).isEmpty();
	}

	/**
	 * @param result        the search result the matches belong to
	 * @param displayedOnly whether matches hidden by an active match filter are
	 *                      ignored
	 * @param maxCount      the collecting stops as soon as this number of matches
	 *                      is reached
	 * @return the matches of this line, at most <code>maxCount</code>
	 */
	private List<FileMatch> collectMatches(AbstractTextSearchResult result, boolean displayedOnly, int maxCount) {
		boolean useFilterState= displayedOnly && result.hasActiveMatchFilters();
		List<FileMatch> res= new ArrayList<>();
		Enumeration<Match> matches= result.getMatchSet(fParent);
		while (matches.hasMoreElements() && res.size() < maxCount) {
			FileMatch curr= (FileMatch) matches.nextElement();
			if (curr.getLineElement() == this && !(useFilterState && curr.isFiltered())) {
				res.add(curr);
			}
		}
		return res;
	}

	private static FileMatch[] toArray(List<FileMatch> matches) {
		return matches.toArray(new FileMatch[matches.size()]);
	}
}
