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

public final class KeyBindingMatch implements Comparable {

	private final static int HASH_INITIAL = 57;
	private final static int HASH_FACTOR = 67;

	static KeyBindingMatch create(KeyBinding keyBinding, int value)
		throws IllegalArgumentException {
		return new KeyBindingMatch(keyBinding, value);
	}
	
	private KeyBinding keyBinding;
	private int value;

	private KeyBindingMatch(KeyBinding keyBinding, int value)
		throws IllegalArgumentException {
		if (keyBinding == null || value < 0)
			throw new IllegalArgumentException();
			
		this.keyBinding = keyBinding;
		this.value = value;
	}

	public int compareTo(Object object) {
		KeyBindingMatch keyBindingMatch = (KeyBindingMatch) object;
		int compareTo = keyBinding.compareTo(keyBindingMatch.keyBinding);
		
		if (compareTo == 0)
			compareTo = value - keyBindingMatch.value;

		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof KeyBindingMatch))
			return false;

		KeyBindingMatch keyBindingMatch = (KeyBindingMatch) object;		
		return keyBinding.equals(keyBindingMatch.keyBinding) && value == keyBindingMatch.value;
	}

	public KeyBinding getKeyBinding() {
		return keyBinding;	
	}
	
	public int getValue() {
		return value;	
	}	

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + keyBinding.hashCode();
		result = result * HASH_FACTOR + value;
		return result;
	}
}
