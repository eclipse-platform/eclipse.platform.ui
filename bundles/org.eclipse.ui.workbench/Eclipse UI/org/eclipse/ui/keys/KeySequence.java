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
 * A <code>KeySequence</code> is defined a set of zero or more 
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
 * <code>KeySequence</code> objects are immutable. It is not permitted to extend 
 * this class.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public final class KeySequence implements Comparable {

	public final static char KEY_STROKE_DELIMITER = '\u0020'; 
	public final static String KEY_STROKE_DELIMITERS = KEY_STROKE_DELIMITER + "\b\r\u007F\u001B\f\n\0\t\u000B"; //$NON-NLS-1$

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = KeySequence.class.getName().hashCode();
	private final static String KEY_STROKE_DELIMITER_KEY = "KEY_STROKE_DELIMITER"; //$NON-NLS-1$
	private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(KeySequence.class.getName());
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 */		
	public static KeySequence getInstance() {
		return new KeySequence(Collections.EMPTY_LIST);
	}

	/**
	 * JAVADOC
	 * 
	 * @param keyStroke
	 * @return
	 */		
	public static KeySequence getInstance(KeyStroke keyStroke) {
		return new KeySequence(Collections.singletonList(keyStroke));
	}

	/**
	 * JAVADOC
	 * 
	 * @param keyStrokes
	 * @return
	 */		
	public static KeySequence getInstance(KeyStroke[] keyStrokes) {
		return new KeySequence(Arrays.asList(keyStrokes));
	}

	/**
	 * JAVADOC
	 * 
	 * @param keyStrokes
	 * @return
	 */		
	public static KeySequence getInstance(List keyStrokes) {
		return new KeySequence(keyStrokes);
	}

	/**
	 * JAVADOC
	 * 
	 * @param string
	 * @return
	 * @throws ParseException
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

	private List keyStrokes;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
		
	private KeySequence(List keyStrokes) {
		this.keyStrokes = Util.safeCopy(keyStrokes, KeyStroke.class);
		
		for (int i = 0; i < this.keyStrokes.size() - 1; i++) {
			KeyStroke keyStroke = (KeyStroke) this.keyStrokes.get(i);
			
			if (!keyStroke.isComplete())
				throw new IllegalArgumentException();
		}
	}

	public int compareTo(Object object) {
		KeySequence keySequence = (KeySequence) object;
		int compareTo = Util.compare(keyStrokes, keySequence.keyStrokes);
		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof KeySequence))
			return false;

		KeySequence keySequence = (KeySequence) object;
		boolean equals = true;
		equals &= keyStrokes.equals(keySequence.keyStrokes);
		return equals;
	}

	/**
	 * JAVADOC
	 * 
	 * @return
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
	 * JAVADOC
	 * 
	 * @return
	 */
	public List getKeyStrokes() {
		return keyStrokes;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + keyStrokes.hashCode();
			hashCodeComputed = true;
		}
			
		return hashCode;
	}

	/**
	 * JAVADOC
	 * 
	 * @param keySequence
	 * @param equals
	 * @return
	 */
	public boolean isChildOf(KeySequence keySequence, boolean equals) {
		if (keySequence == null)
			throw new NullPointerException();
		
		return Util.isChildOf(keyStrokes, keySequence.keyStrokes, equals);
	}

	/**
	 * JAVADOC
	 * 
	 * @return
	 */
	public boolean isComplete() {
		return keyStrokes.isEmpty() || ((KeyStroke) keyStrokes.get(keyStrokes.size() - 1)).isComplete();
	}	
	
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
