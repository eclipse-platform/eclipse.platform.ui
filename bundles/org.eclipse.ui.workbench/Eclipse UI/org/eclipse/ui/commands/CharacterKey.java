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

package org.eclipse.ui.commands;

/**
 * <p>
 * JAVADOC
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public class CharacterKey extends NonModifierKey {

	private static CharacterKey[] cache = new CharacterKey[256];

	/**
	 * JAVADOC
	 * 
	 * @param character
	 * @return
	 */	
	public static CharacterKey getInstance(char character) {
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

	private CharacterKey(char character) {
		super("" + character); // TODO 1.4 Character.toString(character);
	}

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	public char getCharacter() {
		return name.charAt(0);
	}
}
