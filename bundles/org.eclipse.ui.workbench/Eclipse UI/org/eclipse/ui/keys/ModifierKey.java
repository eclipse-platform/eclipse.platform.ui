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

package org.eclipse.ui.keys;

public class ModifierKey extends Key {

	public final static ModifierKey ALT = new ModifierKey("ALT"); 
	public final static ModifierKey COMMAND = new ModifierKey("COMMAND"); 
	public final static ModifierKey CTRL = new ModifierKey("CTRL"); 
	public final static ModifierKey SHIFT = new ModifierKey("SHIFT"); 

	private String name;
	
	private ModifierKey(String name) {
		super();
		this.name = name;
	}

	public String toString() {
		return name;
	}
}
