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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.eclipse.ui.internal.util.Util;

/**
 * <p>
 * A <code>KeySequence</code> is defined as a list of zero or more 
 * <code>KeyStrokes</code>, with the stipulation that all <code>KeyStroke</code>
 * objects must be complete, save for the last one, whose completeness is 
 * optional. A <code>KeySequence</code> is said to be complete if all of its
 * <code>KeyStroke</code> objects are complete.
 * </p>
 * <p>
 * All <code>KeySequence</code> objects have a formal string representation 
 * available via the <code>toString()</code> method. There are a number of 
 * methods to get instances of <code>KeySequence</code> objects, including one 
 * which can parse this formal string representation. 
 * </p>
 * <p>
 * All <code>KeySequence</code> objects, via the <code>format()</code> method, 
 * provide a version of their formal string representation translated by 
 * platform and locale, suitable for display to a user.
 * </p>
 * <p>
 * <code>KeySequence</code> objects are immutable. Clients are not permitted to 
 * extend this class.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public final class KeySequence implements Comparable {

	/**
	 * The delimiter for <code>KeyStrokes</code> objects in the formal string 
	 * representation.
	 */
	public final static char KEY_STROKE_DELIMITER = '\u0020'; 
	
	/**
	 * The set of delimiters for <code>KeyStroke</code> objects allowed during 
	 * parsing of the formal string representation. 
	 */
	public final static String KEY_STROKE_DELIMITERS = KEY_STROKE_DELIMITER + "\b\r\u007F\u001B\f\n\0\t\u000B"; //$NON-NLS-1$

	/**
	 * An internal constant used only in this object's hash code algorithm.
	 */
	private final static int HASH_FACTOR = 89;
	
	/**
	 * An internal constant used only in this object's hash code algorithm.
	 */
	private final static int HASH_INITIAL = KeySequence.class.getName().hashCode();
	
	/**
	 * An internal constant used to find the translation of the key delimiter 
	 * in the resource bundle.
	 */	
	private final static String KEY_STROKE_DELIMITER_KEY = "KEY_STROKE_DELIMITER"; //$NON-NLS-1$
		
	/**
	 * The resource bundle used by <code>format()</code> to translate formal
	 * string representations by locale.
	 */
	private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(KeySequence.class.getName());
	
	/**
	 * Gets an instance of <code>KeySequence</code>. 
	 * 
	 * @return a key sequence. This key sequence will have no key strokes. 
	 */	
	public static KeySequence getInstance() {
		return new KeySequence(Collections.EMPTY_LIST);
	}

	/**
	 * Gets an instance of <code>KeySequence</code> given a single key stroke.
	 * 
	 * @param keyStrokes a key strokes. Must not be <code>null</code>.
	 * @return a key sequence.
	 */	
	public static KeySequence getInstance(KeyStroke keyStroke) {
		return new KeySequence(Collections.singletonList(keyStroke));
	}

	/**
	 * Gets an instance of <code>KeySequence</code> given an array of key 
	 * strokes.
	 * 
	 * @param keyStrokes the array of key strokes. This array may be empty, but
	 *                   it must not be <code>null</code>. This array must not
	 *                   contain <code>null</code> elements.
	 * @return a key sequence.
	 */
	public static KeySequence getInstance(KeyStroke[] keyStrokes) {
		return new KeySequence(Arrays.asList(keyStrokes));
	}

	/**
	 * Gets an instance of <code>KeySequence</code> given a list of key 
	 * strokes.
	 * 
	 * @param keyStrokes the list of key strokes. This list may be empty, but
	 *        			 it must not be <code>null</code>. If this list is not 
	 * 					 empty, it must only contain instances of 
	 * 					 <code>KeyStroke</code>.
	 * @return a key sequence.
	 */
	public static KeySequence getInstance(List keyStrokes) {
		return new KeySequence(keyStrokes);
	}

	/**
	 * Gets an instance of <code>KeySequence</code> by parsing a given a formal
	 * string representation. 
	 * 
	 * @param string the formal string representation to parse.
	 * @return a key sequence.
	 * @throws ParseException if the given formal string representation could
	 * 						  not be parsed to a valid key sequence.
	 */
	public static KeySequence getInstance(String string)
		throws ParseException {
		if (string == null)
			throw new NullPointerException();

		List keyStrokes = new ArrayList();
		StringTokenizer stringTokenizer = new StringTokenizer(string, KEY_STROKE_DELIMITERS);
				
		while (stringTokenizer.hasMoreTokens())
			keyStrokes.add(KeyStroke.getInstance(stringTokenizer.nextToken()));

		try {
			return new KeySequence(keyStrokes);
		} catch (Throwable t) {
			throw new ParseException();
		}		
	}

	/**
	 * The list of key strokes for this key sequence.
	 */
	private List keyStrokes;

	/**
	 * The cached hash code for this object. Because <code>KeySequence</code> 
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
	 * The cached formal string representation for this object. Because 
	 * <code>KeySequence</code> objects are immutable, their formal string 
	 * representations need only to be computed once. After the first call to 
	 * <code>toString()</code>, the computed value is cached here for all 
	 * subsequent calls.
	 */
	private transient String string;	
	
	/**
	 * Constructs an instance of <code>KeySequence</code> given a list of key 
	 * strokes.
	 * 
	 * @param keyStrokes the list of key strokes. This list may be empty, but
	 *        it must not be <code>null</code>. If this list is not empty, it 
	 *        must only contain instances of <code>KeyStroke</code>.
	 */
	private KeySequence(List keyStrokes) {
		this.keyStrokes = Util.safeCopy(keyStrokes, KeyStroke.class);
		
		for (int i = 0; i < this.keyStrokes.size() - 1; i++) {
			KeyStroke keyStroke = (KeyStroke) this.keyStrokes.get(i);
			
			if (!keyStroke.isComplete())
				throw new IllegalArgumentException();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public int compareTo(Object object) {
		KeySequence keySequence = (KeySequence) object;
		int compareTo = Util.compare(keyStrokes, keySequence.keyStrokes);
		return compareTo;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof KeySequence))
			return false;

		KeySequence keySequence = (KeySequence) object;
		boolean equals = true;
		equals &= keyStrokes.equals(keySequence.keyStrokes);
		return equals;
	}

	/**
	 * Returns the formal string representation of this key sequence, translated 
	 * for the user's current platform and locale.
	 * 
	 * @return The formal string representation of this key sequence, translated 
	 *         for the user's current platform and locale. Guaranteed not to be 
	 *         <code>null</code>.
	 */
	public String format() {
		int i = 0;
		String keyStrokeDelimiter = Util.translateString(RESOURCE_BUNDLE, KEY_STROKE_DELIMITER_KEY, Character.toString(KEY_STROKE_DELIMITER), false, false);
		StringBuffer stringBuffer = new StringBuffer();
		
		for (Iterator iterator = keyStrokes.iterator(); iterator.hasNext();) {
			if (i != 0)
				stringBuffer.append(keyStrokeDelimiter);
				
			KeyStroke keyStroke = (KeyStroke) iterator.next();
			stringBuffer.append(keyStroke.format());
			i++;
		}

		return stringBuffer.toString();
	}

	/**
	 * Returns the list of key strokes for this key sequence.
	 * 
	 * @return the list of key strokes keys. This list may be empty, but is 
	 * 		   guaranteed not to be <code>null</code>. If this list is not 
	 *         empty, it is guaranteed to only contain instances of 
	 *         <code>KeyStroke</code>.
	 */
	public List getKeyStrokes() {
		return keyStrokes;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + keyStrokes.hashCode();
			hashCodeComputed = true;
		}
			
		return hashCode;
	}

	/**
	 * Returns whether or not the given key sequence is a child of this key 
	 * sequence. A given key sequence is a child of this key sequence if the 
	 * list of key strokes of the given key sequence start with the list of key 
	 * strokes of this key sequence.
	 * 
	 * @param keySequence a key sequence. Must not be <code>null</code>.
	 * @param equals 	  whether or not an identical key sequence should be 
	 *                    considered a child.
	 * @return <code>true</code>, iff the given key sequence is a child of this
	 * 		   key sequence.
	 */
	public boolean isChildOf(KeySequence keySequence, boolean equals) {
		if (keySequence == null)
			throw new NullPointerException();
		
		return Util.isChildOf(keyStrokes, keySequence.keyStrokes, equals);
	}

	/**
	 * Returns whether or not this key sequence is complete. Key sequences are 
	 * complete iff all of their key strokes are complete.
	 * 
	 * @return <code>true</code>, iff the key sequence is complete. 
	 */
	public boolean isComplete() {
		return keyStrokes.isEmpty() || ((KeyStroke) keyStrokes.get(keyStrokes.size() - 1)).isComplete();
	}	
	
	/**
	 * Returns the formal string representation for this key sequence.
	 * 
	 * @return The formal string representation for this key sequence. 
	 *         Guaranteed not to be <code>null</code>. 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (string == null) {	
		    int i = 0;
			StringBuffer stringBuffer = new StringBuffer();
		
		    for (Iterator iterator = keyStrokes.iterator(); iterator.hasNext();) {
			    if (i != 0)
			        stringBuffer.append(KEY_STROKE_DELIMITER);
				
				KeyStroke keyStroke = (KeyStroke) iterator.next();
				stringBuffer.append(keyStroke.toString());
				i++;
			}

		    string = stringBuffer.toString();
	    }
	
		return string;
	}
}
