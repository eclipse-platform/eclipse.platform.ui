/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.keys;

import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.ui.internal.util.Util;

/**
 * <p>
 * Instances of <code>CharacterKey</code> represent keys on the keyboard which
 * represent unicode characters.
 * </p>
 * <p>
 * <code>CharacterKey</code> objects are immutable. Clients are not permitted to 
 * extend this class.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public final class CharacterKey extends NaturalKey {
	
	/**
	 * The character for BS (U+0008)
	 */
	private final static char BS_CHARACTER = '\b';
	
	/**
	 * An internal cache of the CharacterKey instances representing the first 
	 * 256 unicode characters (Basic Latin and Latin-1 Supplement). This cache
	 * is lazily created by <code>getInstance()</code>.
	 */
	private final static CharacterKey[] CACHE = new CharacterKey[256];	
	
	/**
	 * The character for CR (U+000D)
	 */
	private final static char CR_CHARACTER = '\r';
	
	/**
	 * The character for DEL (U+007F)
	 */
	private final static char DEL_CHARACTER = '\u007F';
	
	/**
	 * The character for ESC (U+001B)
	 */
	private final static char ESC_CHARACTER = '\u001B';
	
	/**
	 * The character for FF (U+000C)
	 */
	private final static char FF_CHARACTER = '\f';
	
	/**
	 * The character for LF (U+000A)
	 */
	private final static char LF_CHARACTER = '\n';
	
	/**
	 * The character for NUL (U+0000)
	 */
	private final static char NUL_CHARACTER = '\0';
	
	/**
	 * The character for SPACE (U+0020)
	 */
	private final static char SPACE_CHARACTER = '\u0020';
	
	/**
	 * The character for TAB (U+0009)
	 */
	private final static char TAB_CHARACTER = '\t';
	
	/**
	 * The character for VT (U+000B)
	 */
	private final static char VT_CHARACTER = '\u000B';	
	
	/**
	 * The single static instance of <code>CharacterKey</code> which represents 
	 * the 'BS' key.
	 */
	public final static CharacterKey BS = CharacterKey.getInstance(BS_CHARACTER); 
	
	/**
	 * The single static instance of <code>CharacterKey</code> which represents 
	 * the 'CR' key.
	 */
	public final static CharacterKey CR = CharacterKey.getInstance(CR_CHARACTER); 
	
	/**
	 * The single static instance of <code>CharacterKey</code> which represents 
	 * the 'DEL' key.
	 */
	public final static CharacterKey DEL = CharacterKey.getInstance(DEL_CHARACTER); 
	
	/**
	 * The single static instance of <code>CharacterKey</code> which represents 
	 * the 'ESC' key.
	 */
	public final static CharacterKey ESC = CharacterKey.getInstance(ESC_CHARACTER);
	
	/**
	 * The single static instance of <code>CharacterKey</code> which represents 
	 * the 'FF' key.
	 */
	public final static CharacterKey FF = CharacterKey.getInstance(FF_CHARACTER);
	
	/**
	 * The single static instance of <code>CharacterKey</code> which represents 
	 * the 'LF' key.
	 */
	public final static CharacterKey LF = CharacterKey.getInstance(LF_CHARACTER); 
	
	/**
	 * The single static instance of <code>CharacterKey</code> which represents 
	 * the 'NUL' key.
	 */
	public final static CharacterKey NUL = CharacterKey.getInstance(NUL_CHARACTER);
	
	/**
	 * The single static instance of <code>CharacterKey</code> which represents 
	 * the 'SPACE' key.
	 */
	public final static CharacterKey SPACE = CharacterKey.getInstance(SPACE_CHARACTER); 
	
	/**
	 * The single static instance of <code>CharacterKey</code> which represents 
	 * the 'TAB' key.
	 */
	public final static CharacterKey TAB = CharacterKey.getInstance(TAB_CHARACTER); 
	
	/**
	 * The single static instance of <code>CharacterKey</code> which represents 
	 * the 'VT' key.
	 */
	public final static CharacterKey VT = CharacterKey.getInstance(VT_CHARACTER); 
	
	/**
	 * The name of the 'BS' key.
	 */
	private final static String BS_NAME = "BS"; //$NON-NLS-1$
	
	/**
	 * The name of the 'CR' key.
	 */
	private final static String CR_NAME = "CR"; //$NON-NLS-1$
	
	/**
	 * The name of the 'DEL' key.
	 */
	private final static String DEL_NAME = "DEL"; //$NON-NLS-1$
	
	/**
	 * The name of the 'ESC' key.
	 */
	private final static String ESC_NAME = "ESC"; //$NON-NLS-1$
	
	/**
	 * The name of the 'FF' key.
	 */
	private final static String FF_NAME = "FF"; //$NON-NLS-1$
	
	/**
	 * The name of the 'LF' key.
	 */
	private final static String LF_NAME = "LF"; //$NON-NLS-1$
	
	/**
	 * The name of the 'NUL' key.
	 */
	private final static String NUL_NAME = "NUL"; //$NON-NLS-1$
	
	/**
	 * The resource bundle used by <code>format()</code> to translate key names
	 * by locale.
	 */
	private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(CharacterKey.class.getName());
	
	/**
	 * The name of the 'SPACE' key.
	 */
	private final static String SPACE_NAME = "SPACE"; //$NON-NLS-1$
	
	/**
	 * The name of the 'TAB' key.
	 */
	private final static String TAB_NAME = "TAB"; //$NON-NLS-1$
	
	/**
	 * The name of the 'VT' key.
	 */
	private final static String VT_NAME = "VT"; //$NON-NLS-1$

	/**
	 * The unicode character represented by this <code>CharacterKey</code> 
	 * instance.
	 */
	private final char character;
	
	/**
	 * Creates an instance of <code>CharacterKey</code> given a unicode 
	 * character. This method determines the correct name for the key based
	 * on character. Typically, this name is a string of one-character in 
	 * length equal to the character that this instance represents.
	 * 
	 * @param  character the character that the resultant 
	 * 					 <code>CharacterKey</code> instance is to represent.
	 * @return 			 an instance of <code>CharacterKey</code> representing 
	 * 					 the character.
	 */	
	public static CharacterKey getInstance(char character) {
		String name;
	
		switch (character) {
			case BS_CHARACTER:
				name = BS_NAME;
				break;

			case CR_CHARACTER:
				name = CR_NAME;
				break;	
				
			case DEL_CHARACTER:
				name = DEL_NAME;
				break;
				
			case ESC_CHARACTER:
				name = ESC_NAME;
				break;
				
			case FF_CHARACTER:
				name = FF_NAME;
				break;
				
			case LF_CHARACTER:
				name = LF_NAME;
				break;	
			
			case NUL_CHARACTER:
				name = NUL_NAME;
				break;

			case SPACE_CHARACTER:
				name = SPACE_NAME;
				break;

			case TAB_CHARACTER:
				name = TAB_NAME;
				break;

			case VT_CHARACTER:
				name = VT_NAME;
				break;			

			default:
				name = Character.toString(character);
				break;
		}
		
		if (character < CACHE.length) {
			CharacterKey characterKey = CACHE[character];
			
			if (characterKey == null) {
				characterKey = new CharacterKey(character, name);
				CACHE[character] = characterKey;
			}
			
			return characterKey;	
		} else
			return new CharacterKey(character, name);
	}

	/**
	 * Constructs an instance of <code>CharacterKey</code> given a unicode 
	 * character and a name. 
	 * 
	 * @param character	the unicode character this object represents.
	 * @param name 		the name of the key, must not be null.
	 */		
	private CharacterKey(char character, String name) {
		super(name);
		this.character = character;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.keys.Key#format()
	 */
	public String format() {
		// TODO consider platform-specific resource bundles		
		if ("carbon".equals(SWT.getPlatform())) { //$NON-NLS-1$    	
			if (BS_NAME.equals(name)) 
				return Character.toString('\u232B');
			
			if (CR_NAME.equals(name)) 
				return Character.toString('\u21A9');
			
			if (DEL_NAME.equals(name))
				return Character.toString('\u2326');
		}

		return Util.translateString(RESOURCE_BUNDLE, name, name, false, false);
	}
	
	/**
	 * Gets the character that this object represents.
	 * 
	 * @return the character that this object represents.
	 */
	public char getCharacter() {
		return character;
	}
}
