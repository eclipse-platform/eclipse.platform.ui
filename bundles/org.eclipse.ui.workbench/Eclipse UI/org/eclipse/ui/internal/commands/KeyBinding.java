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
	private int match;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	KeyBinding(KeySequence keySequence, int match) {	
		if (keySequence == null)
			throw new NullPointerException();
			
		if (match < 0)
			throw new IllegalArgumentException();

		this.keySequence = keySequence;
		this.match = match;
	}

	public int compareTo(Object object) {
		KeyBinding keyBinding = (KeyBinding) object;
		int compareTo = match - keyBinding.match;
		
		if (compareTo == 0)
			compareTo = keySequence.compareTo(keyBinding.keySequence);
					
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof KeyBinding))
			return false;

		KeyBinding keyBinding = (KeyBinding) object;	
		boolean equals = true;
		equals &= keySequence.equals(keyBinding.keySequence);
		equals &= match == keyBinding.match;
		return equals;
	}

	public KeySequence getKeySequence() {
		return keySequence;
	}
		
	public int getMatch() {
		return match;	
	}
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + keySequence.hashCode();
			hashCode = hashCode * HASH_FACTOR + match;			
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(keySequence);
			stringBuffer.append(',');
			stringBuffer.append(match);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;			
	}
}
