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

package org.eclipse.ui.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

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
public class KeySequence implements Comparable {

	private final static char KEY_STROKE_DELIMITER = ' '; //$NON-NLS-1$
	private final static String KEY_STROKE_DELIMITERS = KEY_STROKE_DELIMITER + "\b\t\r\u001b\u007F"; //$NON-NLS-1$

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
			
		return new KeySequence(keyStrokes);
	}

	private List keyStrokes;
	
	private KeySequence(List keyStrokes) {
		super();
		this.keyStrokes = Util.safeCopy(keyStrokes, KeyStroke.class);
	}

	public int compareTo(Object object) {
		return Util.compare(keyStrokes, ((KeySequence) object).keyStrokes);
	}

	public boolean equals(Object object) {
		return object instanceof KeySequence && keyStrokes.equals(((KeySequence) object).keyStrokes);
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
		return keyStrokes.hashCode();
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

	public String toString() {
		int i = 0;
		Iterator iterator = keyStrokes.iterator();
		StringBuffer stringBuffer = new StringBuffer();
			
		while (iterator.hasNext()) {
			if (i != 0)
				stringBuffer.append(KEY_STROKE_DELIMITER);
	
			stringBuffer.append(((KeyStroke) iterator.next()).toString());
			i++;
		}
	
		return stringBuffer.toString();
	}
}
