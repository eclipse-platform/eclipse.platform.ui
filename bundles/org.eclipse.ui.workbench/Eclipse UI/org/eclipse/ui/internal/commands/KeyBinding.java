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

package org.eclipse.ui.internal.commands;

import org.eclipse.ui.commands.IKeyBinding;
import org.eclipse.ui.keys.KeySequence;

final class KeyBinding implements IKeyBinding {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = KeyBinding.class.getName().hashCode();

	private KeySequence keySequence;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	
	KeyBinding(KeySequence keySequence) {	
		if (keySequence == null)
			throw new NullPointerException();

		this.keySequence = keySequence;
	}

	public int compareTo(Object object) {
		KeyBinding keyBinding = (KeyBinding) object;
		int compareTo = keySequence.compareTo(keyBinding.keySequence);			
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof KeyBinding))
			return false;

		KeyBinding keyBinding = (KeyBinding) object;	
		boolean equals = true;
		equals &= keySequence.equals(keyBinding.keySequence);
		return equals;
	}

	public KeySequence getKeySequence() {
		return keySequence;
	}
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + keySequence.hashCode();
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}

	public String toString() {
		return keySequence.toString();		
	}
}
