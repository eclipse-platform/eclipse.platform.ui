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

package org.eclipse.ui.keys;

import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.ui.internal.util.Util;

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
public final class CharacterKey extends NaturalKey {

	private final static char BS_CHARACTER = '\b';
	private final static CharacterKey[] CACHE = new CharacterKey[256];		
	private final static char CR_CHARACTER = '\r';
	private final static char DEL_CHARACTER = '\u007F';
	private final static char ESC_CHARACTER = '\u001B';
	private final static char FF_CHARACTER = '\f';
	private final static char LF_CHARACTER = '\n';
	private final static char NUL_CHARACTER = '\0';
	private final static char SPACE_CHARACTER = '\u0020';
	private final static char TAB_CHARACTER = '\t';
	private final static char VT_CHARACTER = '\u000B';	
	
	public final static CharacterKey BS = CharacterKey.getInstance(BS_CHARACTER); 
	public final static CharacterKey CR = CharacterKey.getInstance(CR_CHARACTER); 
	public final static CharacterKey DEL = CharacterKey.getInstance(DEL_CHARACTER); 
	public final static CharacterKey ESC = CharacterKey.getInstance(ESC_CHARACTER); 
	public final static CharacterKey FF = CharacterKey.getInstance(FF_CHARACTER); 
	public final static CharacterKey LF = CharacterKey.getInstance(LF_CHARACTER); 
	public final static CharacterKey NUL = CharacterKey.getInstance(NUL_CHARACTER); 
	public final static CharacterKey SPACE = CharacterKey.getInstance(SPACE_CHARACTER); 
	public final static CharacterKey TAB = CharacterKey.getInstance(TAB_CHARACTER); 
	public final static CharacterKey VT = CharacterKey.getInstance(VT_CHARACTER); 
	
	private final static String BS_NAME = "BS"; //$NON-NLS-1$
	private final static String CR_NAME = "CR"; //$NON-NLS-1$
	private final static String DEL_NAME = "DEL"; //$NON-NLS-1$
	private final static String ESC_NAME = "ESC"; //$NON-NLS-1$	
	private final static String FF_NAME = "FF"; //$NON-NLS-1$
	private final static String LF_NAME = "LF"; //$NON-NLS-1$
	private final static String NUL_NAME = "NUL"; //$NON-NLS-1$
	private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(CharacterKey.class.getName());
	private final static String SPACE_NAME = "SPACE"; //$NON-NLS-1$
	private final static String TAB_NAME = "TAB"; //$NON-NLS-1$
	private final static String VT_NAME = "VT"; //$NON-NLS-1$

	private final char character;
	
	/**
	 * JAVADOC
	 * 
	 * @param character
	 * @return
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

	private CharacterKey(char character, String name) {
		super(name);
		this.character = character;
	}

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
	
	public char getCharacter() {
		return character;
	}
}
