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

public final class GestureStroke implements Comparable {

	public final static GestureStroke EAST = new GestureStroke(6);  
	public final static GestureStroke NORTH = new GestureStroke(8);
	public final static GestureStroke SOUTH = new GestureStroke(2);
	public final static GestureStroke WEST = new GestureStroke(4);

	public static GestureStroke create(int value) {
		return new GestureStroke(value);
	}

	public static GestureStroke[] create(int[] values)
		throws IllegalArgumentException {
		if (values == null)
			throw new IllegalArgumentException();
					
		GestureStroke[] gestureStrokes = new GestureStroke[values.length];
			
		for (int i = 0; i < values.length; i++)
			gestureStrokes[i] = create(values[i]);
		
		return gestureStrokes;			
	}

	private int value;

	private GestureStroke(int value) {
		super();
		this.value = value;
	}

	public int compareTo(Object object) {
		return value - ((GestureStroke) object).value;
	}
	
	public boolean equals(Object object) {
		return object instanceof GestureStroke && value == ((GestureStroke) object).value;	
	}

	public int getValue() {
		return value;
	}
	
	public int hashCode() {
		return value;	
	}
}
