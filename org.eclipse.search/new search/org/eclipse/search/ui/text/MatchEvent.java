/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.search.ui.text;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.SearchResultEvent;
/**
 * An event describing adding and removing of Matches. This API is preliminary
 * and subject to change at any time.
 * 
 * @since 3.0
 */
public class MatchEvent extends SearchResultEvent {
	private int fKind;
	private Match[] fMatches;
	private Match[] fMatchContainer= new Match[1];
	/**
	 * Constant for a match being added.
	 * 
	 * @see MatchEvent#getKind()
	 */
	public static final int ADDED= 1;
	/**
	 * Constant for a match being removed.
	 * 
	 * @see MatchEvent#getKind()
	 */
	public static final int REMOVED= 2;
	
	private static final Match[] fgEmtpyMatches= new Match[0];
	
	public MatchEvent(ISearchResult searchResult) {
		super(searchResult);
	}
	/**
	 * Tells whether this is a remove or an add.
	 * 
	 * @return One of <code>ADDED</code> or <code>REMOVED</code>.
	 */
	public int getKind() {
		return fKind;
	}
	/**
	 * Returns the concerned match.
	 * 
	 * @return The match this event is about.
	 */
	public Match[] getMatches() {
		if (fMatches != null)
			return fMatches;
		else if (fMatchContainer[0] != null)
			return fMatchContainer;
		else 
			return fgEmtpyMatches;
	}
	/**
	 * @param kind The kind to set.
	 */
	protected void setKind(int kind) {
		fKind= kind;
	}
	/**
	 * @param match The match to set.
	 */
	protected void setMatch(Match match) {
		fMatchContainer[0]= match;
		fMatches= null;
	}

	protected void setMatches(Match[] matches) {
		fMatchContainer[0]= null;
		fMatches= matches;
	}

}
