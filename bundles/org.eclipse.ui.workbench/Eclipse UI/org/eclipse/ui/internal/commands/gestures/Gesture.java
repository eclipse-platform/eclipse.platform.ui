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

package org.eclipse.ui.internal.commands.gestures;

public class Gesture {

	public final static Gesture DOWN = new Gesture("DOWN"); 
	public final static Gesture LEFT = new Gesture("LEFT"); 
	public final static Gesture RIGHT = new Gesture("RIGHT"); 
	public final static Gesture UP = new Gesture("UP"); 

	private String direction;
	
	private Gesture(String direction) {
		super();
		this.direction = direction;
	}

	public String toString() {
		return direction;
	}
}
