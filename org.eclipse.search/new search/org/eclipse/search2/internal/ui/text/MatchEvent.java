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

import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.Match;

/**
 * @author Thomas Mäder
 *
 */
public class MatchEvent extends SearchResultEvent {


	private int fKind;
	private Match fMatch;
	private static ThreadLocal fgInstances= new ThreadLocal();

	public static final int ADDED= 1;
	public static final int REMOVED= 2;

	public MatchEvent(ISearchResult searchResult) {
		super(searchResult);
	}

	public static MatchEvent getSearchResultEvent(int kind, ISearchResult container, Match match) {
		MatchEvent event= (MatchEvent) fgInstances.get();
		if (event == null) {
			event= new MatchEvent(container);
			fgInstances.set(event);
		}
		event.fSearchResult= container;
		event.fKind= kind;
		event.fMatch= match;
		
		return event;
	}
	
	public int getKind() {
		return fKind;
	}

	public Match getMatch() {
		return fMatch;
	}

}
