/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

public final class GestureBindingMatch implements Comparable {

	private final static int HASH_INITIAL = 57;
	private final static int HASH_FACTOR = 67;

	static GestureBindingMatch create(GestureBinding gestureBinding, int value)
		throws IllegalArgumentException {
		return new GestureBindingMatch(gestureBinding, value);
	}
	
	private GestureBinding gestureBinding;
	private int value;

	private GestureBindingMatch(GestureBinding gestureBinding, int value)
		throws IllegalArgumentException {
		if (gestureBinding == null || value < 0)
			throw new IllegalArgumentException();
			
		this.gestureBinding = gestureBinding;
		this.value = value;
	}

	public int compareTo(Object object) {
		GestureBindingMatch gestureBindingMatch = (GestureBindingMatch) object;
		int compareTo = gestureBinding.compareTo(gestureBindingMatch.gestureBinding);
		
		if (compareTo == 0)
			compareTo = value - gestureBindingMatch.value;

		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof GestureBindingMatch))
			return false;

		GestureBindingMatch gestureBindingMatch = (GestureBindingMatch) object;		
		return gestureBinding.equals(gestureBindingMatch.gestureBinding) && value == gestureBindingMatch.value;
	}

	public GestureBinding getGestureBinding() {
		return gestureBinding;	
	}
	
	public int getValue() {
		return value;	
	}	

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + gestureBinding.hashCode();
		result = result * HASH_FACTOR + value;
		return result;
	}
}
