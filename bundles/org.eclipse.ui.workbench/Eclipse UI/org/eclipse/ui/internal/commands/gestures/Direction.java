/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands.gestures;

public class Direction {

	public final static Direction EAST = new Direction("6");  
	public final static Direction NORTH = new Direction("8");
	public final static Direction NORTH_EAST = new Direction("9");
	public final static Direction NORTH_WEST = new Direction("7");
	public final static Direction SOUTH = new Direction("2");
	public final static Direction SOUTH_EAST = new Direction("3");
	public final static Direction SOUTH_WEST = new Direction("1");
	public final static Direction WEST = new Direction("4");
	
	private String direction;
	
	private Direction(String direction) {
		super();
		this.direction = direction;
	}

	public String toString() {
		return direction;	
	}
}
