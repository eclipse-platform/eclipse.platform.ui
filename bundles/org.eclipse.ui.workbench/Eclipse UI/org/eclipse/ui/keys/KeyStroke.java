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
 * A <code>KeyStroke</code> is defined as an optional set of modifier keys 
 * followed optionally by a natural key. A <code>KeyStroke</code> is said to be
 * complete if it contains a natural key.
 * </p>
 * <p>
 * All <code>KeyStroke</code> objects have a formal string representation 
 * available via the <code>toString()</code> method. There are a number of 
 * methods to get instances of <code>KeyStroke</code> objects, including one 
 * which can parse this formal string representation. 
 * </p>
 * <p>
 * All <code>KeyStroke</code> objects, via the <code>format()</code> method, 
 * provide a version of their formal string representation translated by 
 * platform and locale, suitable for display to a user.
 * </p>
 * <p>
 * <code>KeyStroke</code> objects are immutable. Clients are not permitted to 
 * extend this class.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public final class KeyStroke implements Comparable {

	/**
	 * The delimiter for <code>Key</code> objects in the formal string 
	 * representation.
	 */
	public final static char KEY_DELIMITER = '\u002B';
	
	/**
	 * The set of delimiters for <code>Key</code> objects allowed during parsing
	 * of the formal string representation. 
	 */
	public final static String KEY_DELIMITERS = KEY_DELIMITER + Util.ZERO_LENGTH_STRING;

	/**
	 * A comparator to sort modifier keys in the order that they would be 
	 * displayed to a user. This comparator is platform-specific.
	 */
	private final static Comparator modifierKeyComparator = new Comparator() {

		/**
		 * Calculates a rank for a given modifier key.
		 * 
		 * @param modifierKey the modifier key to rank.
		 * @return the rank of this modifier key. This is a non-negative number
		 *         where a lower number suggests a higher rank.
		 */
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

			if ("gtk".equals(platform)) { //$NON-NLS-1$
				// TODO this is order of modifier keys on gnome
				if (ModifierKey.SHIFT.equals(modifierKey))
					return 0;
				
				if (ModifierKey.CTRL.equals(modifierKey))
					return 1;

				if (ModifierKey.ALT.equals(modifierKey))
					return 2;

				/* TODO this is order of modifier keys on kde
				if (ModifierKey.ALT.equals(modifierKey))
					return 0;
				
				if (ModifierKey.CTRL.equals(modifierKey))
					return 1;
           
				if (ModifierKey.SHIFT.equals(modifierKey))
					return 2;
				*/
			}
			
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
		
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
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

	/**
	 * An internal constant used only in this object's hash code algorithm.
	 */
	private final static int HASH_FACTOR = 89;
	
	/**
	 * An internal constant used only in this object's hash code algorithm.
	 */
	private final static int HASH_INITIAL = KeyStroke.class.getName().hashCode();

	/**
	 * An internal constant used to find the translation of the key delimiter 
	 * in the resource bundle.
	 */
	private final static String KEY_DELIMITER_KEY = "KEY_DELIMITER"; //$NON-NLS-1$	
	
	/**
	 * The resource bundle used by <code>format()</code> to translate formal
	 * string representations by locale.
	 */
	private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(KeyStroke.class.getName());

	/**
	 * An internal map used to lookup instances of <code>CharacterKey</code> 
	 * given the formal string representation of a character key.
	 */
	private static SortedMap characterKeyLookup = new TreeMap();

	/**
	 * An internal map used to lookup instances of <code>ModifierKey</code> 
	 * given the formal string representation of a modifier key.
	 */
	private static SortedMap modifierKeyLookup = new TreeMap();

	/**
	 * An internal map used to lookup instances of <code>SpecialKey</code> 
	 * given the formal string representation of a special key.
	 */
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
	 * TODO 
	 * 
	 * @param naturalKey
	 * @return
	 */		
	public static KeyStroke getInstance(NaturalKey naturalKey) {
		return new KeyStroke(Util.EMPTY_SORTED_SET, naturalKey);
	}

	/**
	 * TODO
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
	 * TODO
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
	 * TODO
	 * 
	 * @param modifierKeys
	 * @param naturalKey
	 * @return
	 */
	public static KeyStroke getInstance(SortedSet modifierKeys, NaturalKey naturalKey) {
		return new KeyStroke(modifierKeys, naturalKey);
	}

	/**
	 * TODO
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

	/**
	 * The set of modifier keys for this key stroke.
	 */
	private SortedSet modifierKeys;
	
	/**
	 * The natural key for this key stroke.
	 */
	private NaturalKey naturalKey;

	/**
	 * The cached hash code for this object. Because <code>KeyStroke</code> 
	 * objects are immutable, their hash codes need only to be computed once. 
	 * After the first call to <code>hashCode()</code>, the computed value is 
	 * cached here for all subsequent calls.
	 */
	private transient int hashCode;
	
	/**
	 * A flag to determine if the <code>hashCode</code> field has already been 
	 * computed. 
	 */
	private transient boolean hashCodeComputed;
	
	/**
	 * The set of modifier keys for this key stroke in the form of an array. 
	 * Used internally by <code>int compareTo(Object)</code>. 
	 */	
	private transient ModifierKey[] modifierKeysAsArray;

	/**
	 * The cached formal string representation for this object. Because 
	 * <code>KeyStroke</code> objects are immutable, their formal string 
	 * representations need only to be computed once. After the first call to 
	 * <code>toString()</code>, the computed value is cached here for all 
	 * subsequent calls.
	 */
	private transient String string;
	
	/**
	 * Constructs an instance of <code>KeyStroke</code> given a set of modifier 
	 * keys and a natural key.
	 * 
	 * @param modifierKeys the set of modifier keys. This set may be empty, but
	 *        it must not be <code>null</code>. If this set is not empty, it 
	 *        must only contain instances of <code>ModifierKey</code>.
	 * @param naturalKey the natural key. May be <code>null</code>.
	 */
	private KeyStroke(SortedSet modifierKeys, NaturalKey naturalKey) {
		this.modifierKeys = Util.safeCopy(modifierKeys, ModifierKey.class);
		this.naturalKey = naturalKey;		
		this.modifierKeysAsArray = (ModifierKey[]) this.modifierKeys.toArray(new ModifierKey[this.modifierKeys.size()]);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object object) {
		KeyStroke keyStroke = (KeyStroke) object;
		int compareTo = Util.compare((Comparable[]) modifierKeysAsArray, (Comparable[]) keyStroke.modifierKeysAsArray);
		
		if (compareTo == 0)
			compareTo = Util.compare(naturalKey, keyStroke.naturalKey);			
			
		return compareTo;	
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
	 * Returns the formal string representation for this key stroke, translated 
	 * for the user's current platform and locale.
	 * 
	 * @return The formal string representation for this key stroke, translated 
	 *         for the user's current platform and locale. Guaranteed not to be 
	 *         <code>null</code>.
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
	 * Returns the set of modifier keys for this key stroke.
	 * 
	 * @return the set of modifier keys. This set may be empty, but is 
	 * 		   guaranteed not to be <code>null</code>. If this set is not empty, 
	 *         it is guaranteed to only contain instances of 
	 *         <code>ModifierKey</code>.
     */
	public Set getModifierKeys() {
		return Collections.unmodifiableSet(modifierKeys);
	}

	/**
	 * Returns the natural key for this key stroke.
	 * 
	 * @return the natural key. May be <code>null</code>.
	 */
	public NaturalKey getNaturalKey() {
		return naturalKey;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
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
	 * Returns whether or not this key stroke is complete. Key strokes are 
	 * complete iff they have a natural key which is not <code>null</code>.
	 * 
	 * @return <code>true</code>, iff the key stroke is complete. 
	 */
	public boolean isComplete() {
		return naturalKey != null;
	}	

	/**
	 * Returns the formal string representation for this key stroke.
	 * 
	 * @return The formal string representation for this key stroke. Guaranteed 
	 * 		   not to be <code>null</code>. 
	 * @see java.lang.Object#toString()
	 */
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
