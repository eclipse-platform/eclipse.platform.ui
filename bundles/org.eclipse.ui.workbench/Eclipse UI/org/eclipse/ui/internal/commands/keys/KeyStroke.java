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

public class KeyStroke {

	private ModifierKey[] modifierKeys;
	private NonModifierKey nonModifierKey;
	
	public KeyStroke(ModifierKey[] modifierKeys, NonModifierKey nonModifierKey) {
		super();
		this.modifierKeys = modifierKeys;
		this.nonModifierKey = nonModifierKey;		
	}

	public ModifierKey[] getModifierKeys() {
		return (ModifierKey[]) modifierKeys.clone();
	}

	public NonModifierKey getNonModifierKey() {
		return nonModifierKey;
	}
}
