/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

public final class MatchKeySequence implements Comparable {

	private final static int HASH_INITIAL = 77;
	private final static int HASH_FACTOR = 87;

	static MatchKeySequence create(Match match, KeySequence keySequence)
		throws IllegalArgumentException {
		return new MatchKeySequence(match, keySequence);
	}
	
	private Match match;
	private KeySequence keySequence;

	private MatchKeySequence(Match match, KeySequence keySequence)
		throws IllegalArgumentException {
		if (match == null || keySequence == null)
			throw new IllegalArgumentException();
			
		this.match = match;
		this.keySequence = keySequence;
	}

	public Match getMatch() {
		return match;	
	}	

	public KeySequence getKeySequence() {
		return keySequence;
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof MatchKeySequence))
			throw new ClassCastException();
			
		MatchKeySequence matchKeySequence = (MatchKeySequence) object;
		int compareTo = match.compareTo(matchKeySequence.match);
		
		if (compareTo == 0)
			compareTo = keySequence.compareTo(matchKeySequence.keySequence);			
		
		return compareTo;		
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof MatchKeySequence))
			return false;

		MatchKeySequence matchKeySequence = (MatchKeySequence) object;		
		return match.equals(matchKeySequence.match) && keySequence.equals(matchKeySequence.keySequence);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + match.hashCode();
		result = result * HASH_FACTOR + keySequence.hashCode();
		return result;
	}	
}
