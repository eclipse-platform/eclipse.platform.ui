/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.actions.keybindings;

public final class Match implements Comparable {

	private final static int HASH_INITIAL = 57;
	private final static int HASH_FACTOR = 67;

	static Match create(Binding binding, int value)
		throws IllegalArgumentException {
		return new Match(binding, value);
	}
	
	private Binding binding;
	private int value;

	private Match(Binding binding, int value)
		throws IllegalArgumentException {
		if (binding == null || value < 0)
			throw new IllegalArgumentException();
			
		this.binding = binding;
		this.value = value;
	}

	public Binding getBinding() {
		return binding;	
	}
	
	public int getValue() {
		return value;	
	}	

	public int compareTo(Object object) {
		Match match = (Match) object;
		int compareTo = binding.compareTo(match.binding);
		
		if (compareTo == 0)
			compareTo = value - match.value;

		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Match))
			return false;

		Match match = (Match) object;		
		return binding.equals(match.binding) && value == match.value;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + binding.hashCode();
		result = result * HASH_FACTOR + value;
		return result;
	}
}
