/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

public final class Match implements Comparable {

	private final static int HASH_INITIAL = 57;
	private final static int HASH_FACTOR = 67;

	static Match create(int match, State state)
		throws IllegalArgumentException {
		return new Match(match, state);
	}
	
	private int value;
	private State state;

	private Match(int value, State state)
		throws IllegalArgumentException {
		if (value < 0 || state == null)
			throw new IllegalArgumentException();
			
		this.value = value;
		this.state = state;
	}

	public int getValue() {
		return value;	
	}	

	public State getState() {
		return state;	
	}

	public int compareTo(Object object) {
		if (!(object instanceof Match))
			throw new ClassCastException();
			
		Match match = (Match) object;
		int compareTo = value - match.value;
		
		if (compareTo == 0)
			compareTo = state.compareTo(match.state);

		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Match))
			return false;

		Match match = (Match) object;		
		return value == match.value && state.equals(match.state);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + value;
		result = result * HASH_FACTOR + state.hashCode();
		return result;
	}
}
