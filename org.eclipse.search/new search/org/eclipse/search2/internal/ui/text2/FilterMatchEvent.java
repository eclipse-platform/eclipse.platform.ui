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

import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchEvent;

public class FilterMatchEvent extends MatchEvent {
	private static final long serialVersionUID= -4394594389515651137L;
	public static final int FILTER_CHANGED= 3;

	public FilterMatchEvent(ISearchResult searchResult) {
		super(searchResult);
		setKind(FILTER_CHANGED);
	}

	public void setMatches(Match[] matches) {
		super.setMatches(matches);
	}
}
