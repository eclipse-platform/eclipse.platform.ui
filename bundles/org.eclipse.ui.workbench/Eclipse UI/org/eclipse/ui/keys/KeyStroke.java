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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

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
public final class KeyStroke implements Comparable {

	public final static String ALT = "ALT"; //$NON-NLS-1$
	public final static String ARROW_DOWN = "ARROW_DOWN"; //$NON-NLS-1$
	public final static String ARROW_LEFT = "ARROW_LEFT"; //$NON-NLS-1$
	public final static String ARROW_RIGHT = "ARROW_RIGHT"; //$NON-NLS-1$
	public final static String ARROW_UP = "ARROW_UP"; //$NON-NLS-1$
	public final static String BS = "BS"; //$NON-NLS-1$
	public final static String COMMAND = "COMMAND"; //$NON-NLS-1$
	public final static String CR = "CR"; //$NON-NLS-1$
	public final static String CTRL = "CTRL"; //$NON-NLS-1$
	public final static String DEL = "DEL"; //$NON-NLS-1$
	public final static String END = "END"; //$NON-NLS-1$
	public final static String ESC = "ESC"; //$NON-NLS-1$
	public final static String F1 = "F1"; //$NON-NLS-1$
	public final static String F10 = "F10"; //$NON-NLS-1$
	public final static String F11 = "F11"; //$NON-NLS-1$
	public final static String F12 = "F12"; //$NON-NLS-1$
	public final static String F2 = "F2"; //$NON-NLS-1$
	public final static String F3 = "F3"; //$NON-NLS-1$
	public final static String F4 = "F4"; //$NON-NLS-1$
	public final static String F5 = "F5"; //$NON-NLS-1$
	public final static String F6 = "F6"; //$NON-NLS-1$
	public final static String F7 = "F7"; //$NON-NLS-1$
	public final static String F8 = "F8"; //$NON-NLS-1$
	public final static String F9 = "F9"; //$NON-NLS-1$
	public final static String HOME = "HOME"; //$NON-NLS-1$
	public final static String INSERT = "INSERT"; //$NON-NLS-1$
	public final static char KEY_DELIMITER = '+'; //$NON-NLS-1$
	public final static String KEY_DELIMITERS = KEY_DELIMITER + ""; //$NON-NLS-1$
	public final static String PAGE_DOWN = "PAGE_DOWN"; //$NON-NLS-1$
	public final static String PAGE_UP = "PAGE_UP"; //$NON-NLS-1$
	public final static String PLUS = "PLUS"; //$NON-NLS-1$
	public final static String SHIFT = "SHIFT"; //$NON-NLS-1$
	public final static String SPACE = "SPACE"; //$NON-NLS-1$
	public final static String TAB = "TAB"; //$NON-NLS-1$

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = KeyStroke.class.getName().hashCode();
	
	private static SortedMap escapeKeyLookup = new TreeMap();
	private static SortedMap modifierKeyLookup = new TreeMap();
	private static SortedMap specialKeyLookup = new TreeMap();
	
	static {
		escapeKeyLookup.put(BS, CharacterKey.getInstance('\b'));
		escapeKeyLookup.put(CR, CharacterKey.getInstance('\r'));
		escapeKeyLookup.put(DEL, CharacterKey.getInstance('\u007F'));
		escapeKeyLookup.put(ESC, CharacterKey.getInstance('\u001b'));
		escapeKeyLookup.put(PLUS, CharacterKey.getInstance('+'));
		escapeKeyLookup.put(SPACE, CharacterKey.getInstance(' '));
		escapeKeyLookup.put(TAB, CharacterKey.getInstance('\t'));
		modifierKeyLookup.put(ALT, ModifierKey.ALT);
		modifierKeyLookup.put(COMMAND, ModifierKey.COMMAND);
		modifierKeyLookup.put(CTRL, ModifierKey.CTRL);
		modifierKeyLookup.put(SHIFT, ModifierKey.SHIFT);
		specialKeyLookup.put(ARROW_DOWN, SpecialKey.ARROW_DOWN);
		specialKeyLookup.put(ARROW_LEFT, SpecialKey.ARROW_LEFT);
		specialKeyLookup.put(ARROW_RIGHT, SpecialKey.ARROW_RIGHT);
		specialKeyLookup.put(ARROW_UP, SpecialKey.ARROW_UP);		
		specialKeyLookup.put(END, SpecialKey.END);
		specialKeyLookup.put(F1, SpecialKey.F1);
		specialKeyLookup.put(F10, SpecialKey.F10);
		specialKeyLookup.put(F11, SpecialKey.F11);		
		specialKeyLookup.put(F12, SpecialKey.F12);
		specialKeyLookup.put(F2, SpecialKey.F2);
		specialKeyLookup.put(F3, SpecialKey.F3);
		specialKeyLookup.put(F4, SpecialKey.F4);		
		specialKeyLookup.put(F5, SpecialKey.F5);
		specialKeyLookup.put(F6, SpecialKey.F6);
		specialKeyLookup.put(F7, SpecialKey.F7);
		specialKeyLookup.put(F8, SpecialKey.F8);		
		specialKeyLookup.put(F9, SpecialKey.F9);
		specialKeyLookup.put(HOME, SpecialKey.HOME);
		specialKeyLookup.put(INSERT, SpecialKey.INSERT);
		specialKeyLookup.put(PAGE_DOWN, SpecialKey.PAGE_DOWN);		
		specialKeyLookup.put(PAGE_UP, SpecialKey.PAGE_UP);
	}

	/**
	 * JAVADOC
	 * 
	 * @param naturalKey
	 * @return
	 */		
	public static KeyStroke getInstance(NaturalKey naturalKey) {
		return new KeyStroke(Util.EMPTY_SORTED_SET, naturalKey);
	}

	/**
	 * JAVADOC
	 * 
	 * @param modifierKey
	 * @param naturalKey
	 * @return
	 */
	public static KeyStroke getInstance(ModifierKey modifierKey, NaturalKey naturalKey) {
		if (modifierKey == null)
			throw new NullPointerException();

		return new KeyStroke(new TreeSet(Collections.singletonList(modifierKey)), naturalKey);
	}

	/**
	 * JAVADOC
	 * 
	 * @param modifierKeys
	 * @param naturalKey
	 * @return
	 */
	public static KeyStroke getInstance(ModifierKey[] modifierKeys, NaturalKey naturalKey) {
		Util.assertInstance(modifierKeys, ModifierKey.class);		
		return new KeyStroke(new TreeSet(Arrays.asList(modifierKeys)), naturalKey);
	}

	/**
	 * JAVADOC
	 * 
	 * @param modifierKeys
	 * @param naturalKey
	 * @return
	 */
	public static KeyStroke getInstance(SortedSet modifierKeys, NaturalKey naturalKey) {
		return new KeyStroke(modifierKeys, naturalKey);
	}

	/**
	 * JAVADOC
	 * 
	 * @param string
	 * @return
	 * @throws ParseException
	 */
	public static KeyStroke getInstance(String string)
		throws ParseException {
		if (string == null)
			throw new NullPointerException();

		SortedSet modifierKeys = new TreeSet();
		NaturalKey naturalKey = null;
		StringTokenizer stringTokenizer = new StringTokenizer(string, KEY_DELIMITERS);
		
		while (stringTokenizer.hasMoreTokens()) {
			String name = stringTokenizer.nextToken();
			
			if (stringTokenizer.hasMoreTokens()) {
				name = name.toUpperCase();
				ModifierKey modifierKey = (ModifierKey) modifierKeyLookup.get(name);
				
				if (modifierKey == null || !modifierKeys.add(modifierKey))
					throw new ParseException();
			} else if (name.length() == 1) {
				naturalKey = CharacterKey.getInstance(name.charAt(0));				
				break;
			} else {
				name = name.toUpperCase();
				naturalKey = (NaturalKey) escapeKeyLookup.get(name);
				
				if (naturalKey == null)
					naturalKey = (NaturalKey) specialKeyLookup.get(name);

				if (naturalKey == null)
					throw new ParseException();
				
				break;
			} 					
		}
		
		return new KeyStroke(modifierKeys, naturalKey);
	}

	private SortedSet modifierKeys;
	private NaturalKey naturalKey;

	private transient ModifierKey[] modifierKeysAsArray;
	
	private KeyStroke(SortedSet modifierKeys, NaturalKey naturalKey) {
		super();

		if (naturalKey == null)
			throw new NullPointerException();

		this.modifierKeys = Util.safeCopy(modifierKeys, ModifierKey.class);
		this.naturalKey = naturalKey;		
		this.modifierKeysAsArray = (ModifierKey[]) this.modifierKeys.toArray(new ModifierKey[modifierKeys.size()]);
	}

	public int compareTo(Object object) {
		KeyStroke keyStroke = (KeyStroke) object;
		int compareTo = Util.compare((Comparable[]) modifierKeysAsArray, (Comparable[]) keyStroke.modifierKeysAsArray);
		
		if (compareTo == 0)
			compareTo = naturalKey.compareTo(keyStroke.naturalKey);			
			
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof KeyStroke))
			return false;

		KeyStroke keyStroke = (KeyStroke) object;	
		return modifierKeys.equals(keyStroke.modifierKeys) && naturalKey.equals(keyStroke.naturalKey);
	}

	/**
	 * JAVADOC
	 * 
	 * @return
	 */
	public Set getModifierKeys() {
		return Collections.unmodifiableSet(modifierKeys);
	}

	/**
	 * JAVADOC
	 * 
	 * @return
	 */
	public NaturalKey getNonModifierKey() {
		return naturalKey;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + modifierKeys.hashCode();
		result = result * HASH_FACTOR + naturalKey.hashCode();
		return result;		
	}

	public String toString() {
		Iterator iterator = modifierKeys.iterator();
		StringBuffer stringBuffer = new StringBuffer();
	
		while (iterator.hasNext()) {
			stringBuffer.append(iterator.next().toString());
			stringBuffer.append(KEY_DELIMITER);
		}

		String name = naturalKey.toString();

		if ("\b".equals(name)) //$NON-NLS-1$
			stringBuffer.append(BS);
		else if ("\t".equals(name)) //$NON-NLS-1$
			stringBuffer.append(TAB);
		else if ("\r".equals(name)) //$NON-NLS-1$	
			stringBuffer.append(CR);
		else if ("\u001b".equals(name)) //$NON-NLS-1$	
			stringBuffer.append(ESC);
		else if (" ".equals(name)) //$NON-NLS-1$	
			stringBuffer.append(SPACE);
		else if ("+".equals(name)) //$NON-NLS-1$	
			stringBuffer.append(PLUS);
		else if ("\u007F".equals(name)) //$NON-NLS-1$	
			stringBuffer.append(DEL);
		else
			stringBuffer.append(name);
		
		return stringBuffer.toString();
	}
}
