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

import org.eclipse.jface.action.Action;

public final class Stroke implements Comparable {

	public final static Stroke EAST = new Stroke(6);  
	public final static Stroke NORTH = new Stroke(8);
	public final static Stroke SOUTH = new Stroke(2);
	public final static Stroke WEST = new Stroke(4);

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

	public static Stroke parseKeyStroke(String string)
		throws IllegalArgumentException {
		if (string == null)
			throw new IllegalArgumentException();
		
		int value = Action.convertAccelerator(string);
		
		//TODO uncomment
		//if (value == 0)
		//	throw new IllegalArgumentException();
			
		return create(value);
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

	public String formatKeyStroke() {
		return Action.convertAccelerator(value);
	}

	public int getValue() {
		return value;
	}
	
	public int hashCode() {
		return value;	
	}
}
