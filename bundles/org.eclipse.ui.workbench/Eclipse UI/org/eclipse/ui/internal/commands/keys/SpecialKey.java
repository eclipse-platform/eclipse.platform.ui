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

package org.eclipse.ui.internal.commands.keys;

public class SpecialKey extends NonModifierKey {

	public final static SpecialKey ARROW_DOWN = new SpecialKey("ARROW_DOWN"); 
	public final static SpecialKey ARROW_LEFT = new SpecialKey("ARROW_LEFT"); 
	public final static SpecialKey ARROW_RIGHT = new SpecialKey("ARROW_RIGHT"); 
	public final static SpecialKey ARROW_UP = new SpecialKey("ARROW_UP"); 
	public final static SpecialKey END = new SpecialKey("END"); 
	public final static SpecialKey F1 = new SpecialKey("F1"); 
	public final static SpecialKey F10 = new SpecialKey("F10"); 
	public final static SpecialKey F11 = new SpecialKey("F11"); 
	public final static SpecialKey F12 = new SpecialKey("F12"); 
	public final static SpecialKey F2 = new SpecialKey("F2"); 
	public final static SpecialKey F3 = new SpecialKey("F3"); 
	public final static SpecialKey F4 = new SpecialKey("F4"); 
	public final static SpecialKey F5 = new SpecialKey("F5"); 
	public final static SpecialKey F6 = new SpecialKey("F6"); 
	public final static SpecialKey F7 = new SpecialKey("F7"); 
	public final static SpecialKey F8 = new SpecialKey("F8"); 
	public final static SpecialKey F9 = new SpecialKey("F9"); 
	public final static SpecialKey HOME = new SpecialKey("HOME"); 
	public final static SpecialKey INSERT = new SpecialKey("INSERT");	
	public final static SpecialKey PAGE_DOWN = new SpecialKey("PAGE_DOWN"); 
	public final static SpecialKey PAGE_UP = new SpecialKey("PAGE_UP"); 

	private String name;
	
	private SpecialKey(String name) {
		super();
		this.name = name;
	}

	public String toString() {
		return name;
	}	
}
