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

public final class Stroke implements Comparable {

	public static Stroke create(int value) {
		return new Stroke(value);
	}

	public static Stroke[] create(int[] values)
		throws IllegalArgumentException {
		if (values == null)
			throw new IllegalArgumentException();
					
		Stroke[] strokes = new Stroke[values.length];
			
		for (int i = 0; i < values.length; i++)
			strokes[i] = create(values[i]);
		
		return strokes;			
	}

	private int value;

	private Stroke(int value) {
		super();
		this.value = value;
	}

	public int compareTo(Object object) {
		return value - ((Stroke) object).value;
	}
	
	public boolean equals(Object object) {
		return object instanceof Stroke && value == ((Stroke) object).value;	
	}

	public int getValue() {
		return value;
	}
	
	public int hashCode() {
		return value;	
	}
}
