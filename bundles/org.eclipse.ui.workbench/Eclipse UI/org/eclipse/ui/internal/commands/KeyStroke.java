/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

import org.eclipse.jface.action.Action;

public final class KeyStroke implements Comparable {

	public static KeyStroke create(int value) {
		return new KeyStroke(value);
	}

	public static KeyStroke[] create(int[] values)
		throws IllegalArgumentException {
		if (values == null)
			throw new IllegalArgumentException();
					
		KeyStroke[] keyStrokes = new KeyStroke[values.length];
			
		for (int i = 0; i < values.length; i++)
			keyStrokes[i] = create(values[i]);
		
		return keyStrokes;			
	}

	public static KeyStroke parse(String string)
		throws IllegalArgumentException {
		if (string == null)
			throw new IllegalArgumentException();
		
		int value = Action.convertAccelerator(string);
		
		if (value == 0)
			throw new IllegalArgumentException();
			
		return create(value);
	}

	private int value;

	private KeyStroke(int value) {
		super();
		this.value = value;
	}

	public int compareTo(Object object) {
		return value - ((KeyStroke) object).value;
	}
	
	public boolean equals(Object object) {
		return object instanceof KeyStroke && value == ((KeyStroke) object).value;	
	}

	public int getValue() {
		return value;
	}
	
	public int hashCode() {
		return value;	
	}

	public String toString() {
		return Action.convertAccelerator(value);
	}
}
