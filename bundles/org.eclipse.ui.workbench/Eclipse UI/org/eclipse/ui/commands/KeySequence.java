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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

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
public class KeySequence {

	private final static char KEYSTROKE_DELIMITER = ' '; //$NON-NLS-1$
	private final static String KEYSTROKE_DELIMITERS = "\b\t\r\u001b \u007F"; //$NON-NLS-1$

	/**
	 * JAVADOC
	 * 
	 * @param keyStrokes
	 * @return
	 */	
	public static KeySequence create(List keyStrokes) {
		return new KeySequence(keyStrokes);
	}

	/**
	 * JAVADOC
	 * 
	 * @param string
	 * @return
	 * @throws ParseException
	 */
	public static KeySequence parse(String string)
		throws ParseException {
		if (string == null)
			throw new NullPointerException();

		List keyStrokes = new ArrayList();
		StringTokenizer stringTokenizer = new StringTokenizer(string, KEYSTROKE_DELIMITERS);
				
		while (stringTokenizer.hasMoreTokens())
			keyStrokes.add(KeyStroke.parse(stringTokenizer.nextToken()));
			
		return new KeySequence(keyStrokes);
	}

	private List keyStrokes;
	
	private KeySequence(List keyStrokes) {
		super();
		this.keyStrokes = keyStrokes;
	}

	/**
	 * JAVADOC
	 * 
	 * @return
	 */
	public String format() {
		int i = 0;
		Iterator iterator = keyStrokes.iterator();
		StringBuffer stringBuffer = new StringBuffer();
		
		while (iterator.hasNext()) {
			if (i != 0)
				stringBuffer.append(KEYSTROKE_DELIMITER);

			stringBuffer.append(((KeyStroke) iterator.next()).format());
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
}
