/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.keys;

import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;

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
 * @deprecated Please use org.eclipse.jface.bindings.keys.KeyStroke and
 *             org.eclipse.jface.bindings.keys.KeyLookupFactory
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
	public final static CharacterKey BS;

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the carriage return (U+000D) key
	 */
	public final static CharacterKey CR;

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the delete (U+007F) key.
	 */
	public final static CharacterKey DEL;

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the escape (U+001B) key.
	 */
	public final static CharacterKey ESC;

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the form feed (U+000C) key.
	 */
	public final static CharacterKey FF;

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the line feed (U+000A) key.
	 */
	public final static CharacterKey LF;

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the null (U+0000) key.
	 */
	public final static CharacterKey NUL;

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the space (U+0020) key.
	 */
	public final static CharacterKey SPACE;

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the tab (U+0009) key.
	 */
	public final static CharacterKey TAB;

	/**
	 * The single static instance of <code>CharacterKey</code> which
	 * represents the vertical tab (U+000B) key.
	 */
	public final static CharacterKey VT;

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
		return new CharacterKey(character);
	}

	static {
		final IKeyLookup lookup = KeyLookupFactory.getDefault();
		BS = new CharacterKey(lookup.formalKeyLookup(IKeyLookup.BS_NAME));
		CR = new CharacterKey(lookup.formalKeyLookup(IKeyLookup.CR_NAME));
		DEL = new CharacterKey(lookup.formalKeyLookup(IKeyLookup.DEL_NAME));
		ESC = new CharacterKey(lookup.formalKeyLookup(IKeyLookup.ESC_NAME));
		FF = new CharacterKey(lookup.formalKeyLookup(IKeyLookup.FF_NAME));
		LF = new CharacterKey(lookup.formalKeyLookup(IKeyLookup.LF_NAME));
		NUL = new CharacterKey(lookup.formalKeyLookup(IKeyLookup.NUL_NAME));
		SPACE = new CharacterKey(lookup.formalKeyLookup(IKeyLookup.SPACE_NAME));
		TAB = new CharacterKey(lookup.formalKeyLookup(IKeyLookup.TAB_NAME));
		VT = new CharacterKey(lookup.formalKeyLookup(IKeyLookup.VT_NAME));

		characterKeysByName.put(IKeyLookup.BS_NAME, CharacterKey.BS);
		characterKeysByName.put(IKeyLookup.BACKSPACE_NAME, CharacterKey.BS);
		characterKeysByName.put(IKeyLookup.CR_NAME, CharacterKey.CR);
		characterKeysByName.put(IKeyLookup.ENTER_NAME, CharacterKey.CR);
		characterKeysByName.put(IKeyLookup.RETURN_NAME, CharacterKey.CR);
		characterKeysByName.put(IKeyLookup.DEL_NAME, CharacterKey.DEL);
		characterKeysByName.put(IKeyLookup.DELETE_NAME, CharacterKey.DEL);
		characterKeysByName.put(IKeyLookup.ESC_NAME, CharacterKey.ESC);
		characterKeysByName.put(IKeyLookup.ESCAPE_NAME, CharacterKey.ESC);
		characterKeysByName.put(IKeyLookup.FF_NAME, CharacterKey.FF);
		characterKeysByName.put(IKeyLookup.LF_NAME, CharacterKey.LF);
		characterKeysByName.put(IKeyLookup.NUL_NAME, CharacterKey.NUL);
		characterKeysByName.put(IKeyLookup.SPACE_NAME, CharacterKey.SPACE);
		characterKeysByName.put(IKeyLookup.TAB_NAME, CharacterKey.TAB);
		characterKeysByName.put(IKeyLookup.VT_NAME, CharacterKey.VT);
	}

	/**
	 * Constructs an instance of <code>CharacterKey</code> given a unicode
	 * character and a name.
	 * 
	 * @param key
	 *            The key to be wrapped.
	 */
	private CharacterKey(final int key) {
		super(key);
	}

	/**
	 * Gets the character that this object represents.
	 * 
	 * @return the character that this object represents.
	 */
	public final char getCharacter() {
		return (char) key;
	}
}
