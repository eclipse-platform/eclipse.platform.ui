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

public class KeySequence {

	private KeyStroke[] keyStrokes;
	
	public KeySequence(KeyStroke[] keyStrokes) {
		super();
		this.keyStrokes = keyStrokes;
	}

	public KeyStroke[] getKeyStrokes() {
		return (KeyStroke[]) keyStrokes.clone();
	}
}
