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
public class GestureSequence implements Comparable {

	private final static char GESTURE_STROKE_DELIMITER = ' '; //$NON-NLS-1$
	private final static String GESTURE_STROKE_DELIMITERS = GESTURE_STROKE_DELIMITER + "\b\t\r\u001b\u007F"; //$NON-NLS-1$

	/**
	 * JAVADOC
	 * 
	 * @return
	 */		
	public static GestureSequence getInstance() {
		return new GestureSequence(Collections.EMPTY_LIST);
	}

	/**
	 * JAVADOC
	 * 
	 * @param gestureStroke
	 * @return
	 */		
	public static GestureSequence getInstance(GestureStroke gestureStroke) {
		return new GestureSequence(Collections.singletonList(gestureStroke));
	}

	/**
	 * JAVADOC
	 * 
	 * @param gestureStrokes
	 * @return
	 */		
	public static GestureSequence getInstance(GestureStroke[] gestureStrokes) {
		return new GestureSequence(Arrays.asList(gestureStrokes));
	}

	/**
	 * JAVADOC
	 * 
	 * @param gestureStrokes
	 * @return
	 */		
	public static GestureSequence getInstance(List gestureStrokes) {
		return new GestureSequence(gestureStrokes);
	}

	/**
	 * JAVADOC
	 * 
	 * @param string
	 * @return
	 * @throws ParseException
	 */
	public static GestureSequence parse(String string)
		throws ParseException {
		if (string == null)
			throw new NullPointerException();

		List gestureStrokes = new ArrayList();
		StringTokenizer stringTokenizer = new StringTokenizer(string, GESTURE_STROKE_DELIMITERS);
				
		while (stringTokenizer.hasMoreTokens())
			gestureStrokes.add(GestureStroke.parse(stringTokenizer.nextToken()));
			
		return new GestureSequence(gestureStrokes);
	}

	private List gestureStrokes;
	
	private GestureSequence(List gestureStrokes) {
		super();
		this.gestureStrokes = Util.safeCopy(gestureStrokes, GestureStroke.class);
	}

	public int compareTo(Object object) {
		return Util.compare(gestureStrokes, ((GestureSequence) object).gestureStrokes);
	}

	public boolean equals(Object object) {
		return object instanceof GestureSequence && gestureStrokes.equals(((GestureSequence) object).gestureStrokes);
	}

	/**
	 * JAVADOC
	 * 
	 * @return
	 */
	public List getGestureStrokes() {
		return gestureStrokes;
	}

	public int hashCode() {
		return gestureStrokes.hashCode();
	}

	/**
	 * JAVADOC
	 * 
	 * @param gestureSequence
	 * @param equals
	 * @return
	 */
	public boolean isChildOf(GestureSequence gestureSequence, boolean equals) {
		if (gestureSequence == null)
			throw new NullPointerException();
		
		return Util.isChildOf(gestureStrokes, gestureSequence.gestureStrokes, equals);
	}

	public String toString() {
		int i = 0;
		Iterator iterator = gestureStrokes.iterator();
		StringBuffer stringBuffer = new StringBuffer();
			
		while (iterator.hasNext()) {
			if (i != 0)
				stringBuffer.append(GESTURE_STROKE_DELIMITER);
	
			stringBuffer.append(((GestureStroke) iterator.next()).toString());
			i++;
		}
	
		return stringBuffer.toString();
	}
}
