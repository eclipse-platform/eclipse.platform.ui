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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

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
public final class KeyStroke implements Comparable {

	public final static char KEY_DELIMITER = '\u002B';
	public final static String KEY_DELIMITERS = KEY_DELIMITER + Util.ZERO_LENGTH_STRING;
		
	private final static Comparator modifierKeyComparator = new Comparator() {

		private int rank(ModifierKey modifierKey) {
			String platform = SWT.getPlatform();
			
			if ("carbon".equals(platform)) { //$NON-NLS-1$
				if (ModifierKey.SHIFT.equals(modifierKey))
					return 0;
				
				if (ModifierKey.CTRL.equals(modifierKey))
					return 1;

				if (ModifierKey.ALT.equals(modifierKey))
					return 2;

				if (ModifierKey.COMMAND.equals(modifierKey))
					return 3;
			}

			// TODO this is order of modifier keys on gnome
			if ("gtk".equals(platform)) { //$NON-NLS-1$
				if (ModifierKey.SHIFT.equals(modifierKey))
					return 0;
				
				if (ModifierKey.CTRL.equals(modifierKey))
					return 1;

				if (ModifierKey.ALT.equals(modifierKey))
					return 2;
			}
			
			/* TODO this is order of modifier keys on kde
			if ("gtk".equals(platform)) { //$NON-NLS-1$
				if (ModifierKey.ALT.equals(modifierKey))
					return 0;
				
				if (ModifierKey.CTRL.equals(modifierKey))
					return 1;
           
				if (ModifierKey.SHIFT.equals(modifierKey))
					return 2;
			}
			*/

			if ("win32".equals(platform)) { //$NON-NLS-1$
				if (ModifierKey.CTRL.equals(modifierKey))
					return 0;

				if (ModifierKey.ALT.equals(modifierKey))
					return 1;
				
				if (ModifierKey.SHIFT.equals(modifierKey))
					return 2;
			}
			
			return Integer.MAX_VALUE;
		}
		
		public int compare(Object left, Object right) {
			ModifierKey modifierKeyLeft = (ModifierKey) left;
			ModifierKey modifierKeyRight = (ModifierKey) right;
			int modifierKeyLeftRank = rank(modifierKeyLeft);
			int modifierKeyRightRank = rank(modifierKeyRight);

			if (modifierKeyLeftRank != modifierKeyRightRank)
				return modifierKeyLeftRank - modifierKeyRightRank;
			else
				return modifierKeyLeft.compareTo(modifierKeyRight);
		}
	};		

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = KeyStroke.class.getName().hashCode();
	private final static String KEY_DELIMITER_KEY = "KEY_DELIMITER"; //$NON-NLS-1$	
	private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(KeyStroke.class.getName());
	
	private static SortedMap characterKeyLookup = new TreeMap();
	private static SortedMap modifierKeyLookup = new TreeMap();
	private static SortedMap specialKeyLookup = new TreeMap();
	
	static {
		characterKeyLookup.put(CharacterKey.BS.toString(), CharacterKey.BS);
		characterKeyLookup.put(CharacterKey.CR.toString(), CharacterKey.CR);
		characterKeyLookup.put(CharacterKey.DEL.toString(), CharacterKey.DEL);
		characterKeyLookup.put(CharacterKey.ESC.toString(), CharacterKey.ESC);
		characterKeyLookup.put(CharacterKey.FF.toString(), CharacterKey.FF);
		characterKeyLookup.put(CharacterKey.LF.toString(), CharacterKey.LF);
		characterKeyLookup.put(CharacterKey.NUL.toString(), CharacterKey.NUL);
		characterKeyLookup.put(CharacterKey.SPACE.toString(), CharacterKey.SPACE);
		characterKeyLookup.put(CharacterKey.TAB.toString(), CharacterKey.TAB);
		characterKeyLookup.put(CharacterKey.VT.toString(), CharacterKey.VT);
		modifierKeyLookup.put(ModifierKey.ALT.toString(), ModifierKey.ALT);
		modifierKeyLookup.put(ModifierKey.COMMAND.toString(), ModifierKey.COMMAND);
		modifierKeyLookup.put(ModifierKey.CTRL.toString(), ModifierKey.CTRL);
		modifierKeyLookup.put(ModifierKey.SHIFT.toString(), ModifierKey.SHIFT);
		specialKeyLookup.put(SpecialKey.ARROW_DOWN.toString(), SpecialKey.ARROW_DOWN);
		specialKeyLookup.put(SpecialKey.ARROW_LEFT.toString(), SpecialKey.ARROW_LEFT);
		specialKeyLookup.put(SpecialKey.ARROW_RIGHT.toString(), SpecialKey.ARROW_RIGHT);
		specialKeyLookup.put(SpecialKey.ARROW_UP.toString(), SpecialKey.ARROW_UP);		
		specialKeyLookup.put(SpecialKey.END.toString(), SpecialKey.END);
		specialKeyLookup.put(SpecialKey.F1.toString(), SpecialKey.F1);
		specialKeyLookup.put(SpecialKey.F10.toString(), SpecialKey.F10);
		specialKeyLookup.put(SpecialKey.F11.toString(), SpecialKey.F11);		
		specialKeyLookup.put(SpecialKey.F12.toString(), SpecialKey.F12);
		specialKeyLookup.put(SpecialKey.F2.toString(), SpecialKey.F2);
		specialKeyLookup.put(SpecialKey.F3.toString(), SpecialKey.F3);
		specialKeyLookup.put(SpecialKey.F4.toString(), SpecialKey.F4);		
		specialKeyLookup.put(SpecialKey.F5.toString(), SpecialKey.F5);
		specialKeyLookup.put(SpecialKey.F6.toString(), SpecialKey.F6);
		specialKeyLookup.put(SpecialKey.F7.toString(), SpecialKey.F7);
		specialKeyLookup.put(SpecialKey.F8.toString(), SpecialKey.F8);		
		specialKeyLookup.put(SpecialKey.F9.toString(), SpecialKey.F9);
		specialKeyLookup.put(SpecialKey.HOME.toString(), SpecialKey.HOME);
		specialKeyLookup.put(SpecialKey.INSERT.toString(), SpecialKey.INSERT);
		specialKeyLookup.put(SpecialKey.PAGE_DOWN.toString(), SpecialKey.PAGE_DOWN);		
		specialKeyLookup.put(SpecialKey.PAGE_UP.toString(), SpecialKey.PAGE_UP);
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
		StringTokenizer stringTokenizer = new StringTokenizer(string, KEY_DELIMITERS, true);
		int i = 0;
		
		while (stringTokenizer.hasMoreTokens()) {
			String token = stringTokenizer.nextToken();
		
			if (i % 2 == 0) {
				if (stringTokenizer.hasMoreTokens()) {
					token = token.toUpperCase();
					ModifierKey modifierKey = (ModifierKey) modifierKeyLookup.get(token);
				
					if (modifierKey == null || !modifierKeys.add(modifierKey))
						throw new ParseException();
				} else if (token.length() == 1) {
					naturalKey = CharacterKey.getInstance(token.charAt(0));				
					break;
				} else {
					token = token.toUpperCase();
					naturalKey = (NaturalKey) characterKeyLookup.get(token);
				
					if (naturalKey == null)
						naturalKey = (NaturalKey) specialKeyLookup.get(token);

					if (naturalKey == null)
						throw new ParseException();
				}					
			}
		
			i++;
		}
		
		try {
			return new KeyStroke(modifierKeys, naturalKey);
		} catch (Throwable t) {
			throw new ParseException();
		}
	}

	private SortedSet modifierKeys;
	private NaturalKey naturalKey;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient ModifierKey[] modifierKeysAsArray;
	private transient String string;
	
	private KeyStroke(SortedSet modifierKeys, NaturalKey naturalKey) {
		this.modifierKeys = Util.safeCopy(modifierKeys, ModifierKey.class);
		this.naturalKey = naturalKey;		
		this.modifierKeysAsArray = (ModifierKey[]) this.modifierKeys.toArray(new ModifierKey[this.modifierKeys.size()]);
	}

	public int compareTo(Object object) {
		KeyStroke keyStroke = (KeyStroke) object;
		int compareTo = Util.compare((Comparable[]) modifierKeysAsArray, (Comparable[]) keyStroke.modifierKeysAsArray);
		
		if (compareTo == 0)
			compareTo = Util.compare(naturalKey, keyStroke.naturalKey);			
			
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof KeyStroke))
			return false;

		KeyStroke keyStroke = (KeyStroke) object;	
		boolean equals = true;
		equals &= modifierKeys.equals(keyStroke.modifierKeys);
		equals &= Util.equals(naturalKey, keyStroke.naturalKey);		
		return equals;
	}

	/**
	 * JAVADOC
	 * 
	 * @return
	 */
	public String format() {
		// TODO consider platform-specific resource bundles
		String keyDelimiter = "carbon".equals(SWT.getPlatform()) ? Util.ZERO_LENGTH_STRING : Util.translateString(RESOURCE_BUNDLE, KEY_DELIMITER_KEY, Character.toString(KEY_DELIMITER), false, false); //$NON-NLS-1$
		SortedSet modifierKeys = new TreeSet(modifierKeyComparator);
		modifierKeys.addAll(this.modifierKeys);
		StringBuffer stringBuffer = new StringBuffer();
		
		for (Iterator iterator = modifierKeys.iterator(); iterator.hasNext();) {
			ModifierKey modifierKey = (ModifierKey) iterator.next();
			stringBuffer.append(modifierKey.format());
			stringBuffer.append(keyDelimiter);
		}

		if (naturalKey != null)
			stringBuffer.append(naturalKey.format());

		return stringBuffer.toString();
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
	public NaturalKey getNaturalKey() {
		return naturalKey;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + modifierKeys.hashCode();
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(naturalKey);
			hashCodeComputed = true;
		}
			
		return hashCode;
	}

	/**
	 * JAVADOC
	 * 
	 * @return
	 */
	public boolean isComplete() {
		return naturalKey != null;
	}	
	
	public String toString() {
		if (string == null) {
			StringBuffer stringBuffer = new StringBuffer();
		
			for (Iterator iterator = modifierKeys.iterator(); iterator.hasNext();) {
				ModifierKey modifierKey = (ModifierKey) iterator.next();
				stringBuffer.append(modifierKey);
				stringBuffer.append(KEY_DELIMITER);
			}

			if (naturalKey != null)
				stringBuffer.append(naturalKey);

			string = stringBuffer.toString();
		}
	
		return string;
	}
}
