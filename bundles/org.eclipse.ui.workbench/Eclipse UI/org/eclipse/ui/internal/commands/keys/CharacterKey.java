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

public class CharacterKey extends NonModifierKey {

	private static CharacterKey[] cache = new CharacterKey[256];

	public static CharacterKey create(char character) {
		if (character <= 255) {
			CharacterKey characterKey = cache[character];
			
			if (characterKey == null) {
				characterKey = new CharacterKey(character);
				cache[character] = characterKey;
			}
			
			return characterKey;	
		} else
			return new CharacterKey(character);
	}

	private char character;
	
	private CharacterKey(char character) {
		super();
		this.character = character;
	}

	public char getCharacter() {
		return character;
	}

	public String toString() {
		return "" + character; // TODO 1.4 Character.toString(character);
	}
}
