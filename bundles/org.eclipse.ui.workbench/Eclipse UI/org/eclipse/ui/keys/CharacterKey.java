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
 * @since 3.0
 */
public final class CharacterKey extends NaturalKey {

    /**
     * An internal map used to lookup instances of <code>CharacterKey</code>
     * given the formal string representation of a character key.
     */
    static SortedMap characterKeysByName = new TreeMap();

    /**
     * An internal cache of the CharacterKey instances representing the first
     * 256 unicode characters (Basic Latin and Latin-1 Supplement). This cache
     * is lazily created by <code>getInstance()</code>.
     */
    private final static CharacterKey[] CACHE = new CharacterKey[256];

    /**
     * The character for backspace (U+0008).
     */
    private final static char BS_CHARACTER = '\b';

    /**
     * The single static instance of <code>CharacterKey</code> which
     * represents the backspace key (U+0008).
     */
    public final static CharacterKey BS = CharacterKey
            .getInstance(BS_CHARACTER);

    /**
     * The name of the backspace key.
     */
    private final static String BS_NAME = "BS"; //$NON-NLS-1$

    /**
     * The character for carriage return (U+000D)
     */
    private final static char CR_CHARACTER = '\r';

    /**
     * The single static instance of <code>CharacterKey</code> which
     * represents the carriage return (U+000D) key
     */
    public final static CharacterKey CR = CharacterKey
            .getInstance(CR_CHARACTER);

    /**
     * The name of the carriage return (U+000D)
     */
    private final static String CR_NAME = "CR"; //$NON-NLS-1$

    /**
     * The character for delete (U+007F)
     */
    private final static char DEL_CHARACTER = '\u007F';

    /**
     * The single static instance of <code>CharacterKey</code> which
     * represents the delete (U+007F) key.
     */
    public final static CharacterKey DEL = CharacterKey
            .getInstance(DEL_CHARACTER);

    /**
     * The name of the delete (U+007F)
     */
    private final static String DEL_NAME = "DEL"; //$NON-NLS-1$

    /**
     * The character for escape (U+001B)
     */
    private final static char ESC_CHARACTER = '\u001B';

    /**
     * The single static instance of <code>CharacterKey</code> which
     * represents the escape (U+001B) key.
     */
    public final static CharacterKey ESC = CharacterKey
            .getInstance(ESC_CHARACTER);

    /**
     * The name of the escape (U+001B) key.
     */
    private final static String ESC_NAME = "ESC"; //$NON-NLS-1$

    /**
     * The character for form feed (U+000C)
     */
    private final static char FF_CHARACTER = '\f';

    /**
     * The single static instance of <code>CharacterKey</code> which
     * represents the form feed (U+000C) key.
     */
    public final static CharacterKey FF = CharacterKey
            .getInstance(FF_CHARACTER);

    /**
     * The name of the form feed (U+000C) key.
     */
    private final static String FF_NAME = "FF"; //$NON-NLS-1$

    /**
     * The character for line feed (U+000A)
     */
    private final static char LF_CHARACTER = '\n';

    /**
     * The single static instance of <code>CharacterKey</code> which
     * represents the line feed (U+000A) key.
     */
    public final static CharacterKey LF = CharacterKey
            .getInstance(LF_CHARACTER);

    /**
     * The name of the line feed (U+000A) key.
     */
    private final static String LF_NAME = "LF"; //$NON-NLS-1$

    /**
     * The character for null (U+0000)
     */
    private final static char NUL_CHARACTER = '\0';

    /**
     * The single static instance of <code>CharacterKey</code> which
     * represents the null (U+0000) key.
     */
    public final static CharacterKey NUL = CharacterKey
            .getInstance(NUL_CHARACTER);

    /**
     * The name of the null (U+0000) key.
     */
    private final static String NUL_NAME = "NUL"; //$NON-NLS-1$

    /**
     * The character for space (U+0020)
     */
    private final static char SPACE_CHARACTER = '\u0020';

    /**
     * The single static instance of <code>CharacterKey</code> which
     * represents the space (U+0020) key.
     */
    public final static CharacterKey SPACE = CharacterKey
            .getInstance(SPACE_CHARACTER);

    /**
     * The name of the space (U+0020) key.
     */
    private final static String SPACE_NAME = "SPACE"; //$NON-NLS-1$

    /**
     * The character for tab (U+0009)
     */
    private final static char TAB_CHARACTER = '\t';

    /**
     * The single static instance of <code>CharacterKey</code> which
     * represents the tab (U+0009) key.
     */
    public final static CharacterKey TAB = CharacterKey
            .getInstance(TAB_CHARACTER);

    /**
     * The name of the tab (U+0009) key.
     */
    private final static String TAB_NAME = "TAB"; //$NON-NLS-1$

    /**
     * The character for vertical tab (U+000B)
     */
    private final static char VT_CHARACTER = '\u000B';

    /**
     * The single static instance of <code>CharacterKey</code> which
     * represents the vertical tab (U+000B) key.
     */
    public final static CharacterKey VT = CharacterKey
            .getInstance(VT_CHARACTER);

    /**
     * The name of the vertical tab (U+000B) key.
     */
    private final static String VT_NAME = "VT"; //$NON-NLS-1$

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
     * The unicode character represented by this <code>CharacterKey</code>
     * instance.
     */
    private final char character;

    /**
     * Constructs an instance of <code>CharacterKey</code> given a unicode
     * character and a name.
     * 
     * @param character
     *            the unicode character this object represents.
     * @param name
     *            the name of the key, must not be null.
     */
    private CharacterKey(char character, String name) {
        super(name);
        this.character = character;
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
