/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.commands;

/**
 * <p>
 * JAVADOC
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public class Gesture {

	public final static Gesture EAST = new Gesture("EAST"); 
	public final static Gesture NORTH = new Gesture("NORTH"); 
	public final static Gesture SOUTH = new Gesture("SOUTH"); 
	public final static Gesture WEST = new Gesture("WEST"); 

	private String direction;
	
	private Gesture(String direction) {
		super();
		this.direction = direction;
	}

	public String toString() {
		return direction;
	}
}
