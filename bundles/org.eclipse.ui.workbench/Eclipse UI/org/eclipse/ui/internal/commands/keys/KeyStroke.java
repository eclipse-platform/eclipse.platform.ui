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

import java.util.Set;

public class KeyStroke {
	
	public static KeyStroke create(Set modifierKeys, NonModifierKey nonModifierKey) {
		return new KeyStroke(modifierKeys, nonModifierKey);
	}

	private Set modifierKeys;
	private NonModifierKey nonModifierKey;
	
	private KeyStroke(Set modifierKeys, NonModifierKey nonModifierKey) {
		super();
		this.modifierKeys = modifierKeys;
		this.nonModifierKey = nonModifierKey;		
	}

	public Set getModifierKeys() {
		return modifierKeys;
	}

	public NonModifierKey getNonModifierKey() {
		return nonModifierKey;
	}
}
