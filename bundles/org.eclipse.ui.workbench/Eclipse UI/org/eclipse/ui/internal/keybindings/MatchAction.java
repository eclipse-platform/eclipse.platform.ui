/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

public final class MatchAction implements Comparable {

	private final static int HASH_INITIAL = 47;
	private final static int HASH_FACTOR = 57;

	static MatchAction create(Match match, String action)
		throws IllegalArgumentException {
		return new MatchAction(match, action);
	}
	
	private Match match;
	private String action;

	private MatchAction(Match match, String action)
		throws IllegalArgumentException {
		if (match == null || action == null)
			throw new IllegalArgumentException();
			
		this.match = match;
		this.action = action;
	}

	public Match getMatch() {
		return match;	
	}	

	public String getAction() {
		return action;
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof MatchAction))
			throw new ClassCastException();
			
		MatchAction matchAction = (MatchAction) object;
		int compareTo = match.compareTo(matchAction.match);
		
		if (compareTo == 0)
			compareTo = action.compareTo(matchAction.action);	
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof MatchAction))
			return false;

		MatchAction matchAction = (MatchAction) object;		
		return match.equals(matchAction.match) && action.equals(matchAction.action);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + match.hashCode();
		result = result * HASH_FACTOR + action.hashCode();
		return result;
	}
}
