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

public class Direction {

	public final static Direction DOWN = new Direction("DOWN"); 
	public final static Direction LEFT = new Direction("LEFT"); 
	public final static Direction RIGHT = new Direction("RIGHT"); 
	public final static Direction UP = new Direction("UP"); 

	private String string;
	
	private Direction(String string) {
		super();
		this.string = string;
	}

	public String toString() {
		return string;
	}
}
