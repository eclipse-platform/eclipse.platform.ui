/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.keys;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * <p>
 * Instances of <code>CharacterKey</code> represent keys on the keyboard which
 * represent unicode characters.
 * </p>
 * <p>
 * <code>CharacterKey</code> objects are immutable. Clients are not permitted
 * to extend this class.
 * </p>
 * 
 * @deprecated Please use org.eclipse.jface.bindings.keys.CharacterKey
 * @since 3.0
 */
public final class CharacterKey extends NaturalKey {

	/**
	 * An internal map used to lookup instances of <code>CharacterKey</code>
	 * given the formal string representation of a character key.
	 */
	static SortedMap characterKeysByName = new TreeMap();

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the backspace key (U+0008).
	 */
	public final static CharacterKey BS = new CharacterKey(
			org.eclipse.jface.bindings.keys.CharacterKey.BS);

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the carriage return (U+000D) key
	 */
	public final static CharacterKey CR = new CharacterKey(
			org.eclipse.jface.bindings.keys.CharacterKey.CR);

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the delete (U+007F) key.
	 */
	public final static CharacterKey DEL = new CharacterKey(
			org.eclipse.jface.bindings.keys.CharacterKey.DEL);

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the escape (U+001B) key.
	 */
	public final static CharacterKey ESC = new CharacterKey(
			org.eclipse.jface.bindings.keys.CharacterKey.ESC);

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the form feed (U+000C) key.
	 */
	public final static CharacterKey FF = new CharacterKey(
			org.eclipse.jface.bindings.keys.CharacterKey.FF);

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the line feed (U+000A) key.
	 */
	public final static CharacterKey LF = new CharacterKey(
			org.eclipse.jface.bindings.keys.CharacterKey.LF);

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the null (U+0000) key.
	 */
	public final static CharacterKey NUL = new CharacterKey(
			org.eclipse.jface.bindings.keys.CharacterKey.NUL);

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the space (U+0020) key.
	 */
	public final static CharacterKey SPACE = new CharacterKey(
			org.eclipse.jface.bindings.keys.CharacterKey.SPACE);

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the tab (U+0009) key.
	 */
	public final static CharacterKey TAB = new CharacterKey(
			org.eclipse.jface.bindings.keys.CharacterKey.TAB);

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the vertical tab (U+000B) key.
	 */
	public final static CharacterKey VT = new CharacterKey(
			org.eclipse.jface.bindings.keys.CharacterKey.VT);

	/**
	 * Creates an instance of <code>CharacterKey</code> given a unicode
	 * character. This method determines the correct name for the key based on
	 * character. Typically, this name is a string of one-character in length
	 * equal to the character that this instance represents.
	 * 
	 * @param character
	 *            the character that the resultant <code>CharacterKey</code>
	 *            instance is to represent.
	 * @return an instance of <code>CharacterKey</code> representing the
	 *         character.
	 */
	public static final CharacterKey getInstance(final char character) {
		return new CharacterKey(org.eclipse.jface.bindings.keys.CharacterKey
				.getInstance(character));
	}

	static {
		characterKeysByName.put(CharacterKey.BS.toString(), CharacterKey.BS);
		characterKeysByName.put(CharacterKey.CR.toString(), CharacterKey.CR);
		characterKeysByName.put(CharacterKey.DEL.toString(), CharacterKey.DEL);
		characterKeysByName.put(CharacterKey.ESC.toString(), CharacterKey.ESC);
		characterKeysByName.put(CharacterKey.FF.toString(), CharacterKey.FF);
		characterKeysByName.put(CharacterKey.LF.toString(), CharacterKey.LF);
		characterKeysByName.put(CharacterKey.NUL.toString(), CharacterKey.NUL);
		characterKeysByName.put(CharacterKey.SPACE.toString(),
				CharacterKey.SPACE);
		characterKeysByName.put(CharacterKey.TAB.toString(), CharacterKey.TAB);
		characterKeysByName.put(CharacterKey.VT.toString(), CharacterKey.VT);
	}

	/**
	 * Constructs an instance of <code>CharacterKey</code> given a unicode
	 * character and a name.
	 * 
	 * @param key
	 *            The key to be wrapped; must not be <code>null</code>.
	 */
	private CharacterKey(final org.eclipse.jface.bindings.keys.CharacterKey key) {
		super(key);
	}

	/**
	 * Gets the character that this object represents.
	 * 
	 * @return the character that this object represents.
	 */
	public final char getCharacter() {
		return ((org.eclipse.jface.bindings.keys.CharacterKey) key)
				.getCharacter();
	}
}