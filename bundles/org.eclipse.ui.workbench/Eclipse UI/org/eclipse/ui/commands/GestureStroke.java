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

import java.util.SortedMap;
import java.util.TreeMap;

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
public class GestureStroke implements Comparable {

	public final static String EAST = "EAST"; //$NON-NLS-1$
	public final static String NORTH = "NORTH"; //$NON-NLS-1$
	public final static String SOUTH = "SOUTH"; //$NON-NLS-1$
	public final static String WEST = "WEST"; //$NON-NLS-1$

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = GestureStroke.class.getName().hashCode();
	
	private static SortedMap gestureLookup = new TreeMap();
	
	static {
		gestureLookup.put(EAST, Gesture.EAST);
		gestureLookup.put(NORTH, Gesture.NORTH);
		gestureLookup.put(SOUTH, Gesture.SOUTH);
		gestureLookup.put(WEST, Gesture.WEST);
	}

	/**
	 * JAVADOC
	 * 
	 * @param gesture
	 * @return
	 */		
	public static GestureStroke getInstance(Gesture gesture) {
		return new GestureStroke(gesture);
	}

	/**
	 * JAVADOC
	 * 
	 * @param string
	 * @return
	 * @throws ParseException
	 */
	public static GestureStroke parse(String string)
		throws ParseException {
		if (string == null)
			throw new NullPointerException();
			
		String name = string.toUpperCase();
		Gesture gesture = (Gesture) gestureLookup.get(name);
		
		if (gesture == null)
			throw new ParseException();
			
		return new GestureStroke(gesture);	
	}

	private Gesture gesture;
	
	private GestureStroke(Gesture gesture) {
		super();

		if (gesture == null)
			throw new NullPointerException();

		this.gesture = gesture;
	}

	public int compareTo(Object object) {
		GestureStroke gestureStroke = (GestureStroke) object;
		int compareTo = gesture.compareTo(gestureStroke.gesture);
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof GestureStroke))
			return false;

		GestureStroke gestureStroke = (GestureStroke) object;	
		return gesture.equals(gestureStroke.gesture);
	}

	/**
	 * JAVADOC
	 * 
	 * @return
	 */
	public Gesture getGesture() {
		return gesture;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + gesture.hashCode();
		return result;		
	}

	public String toString() {
		return gesture.toString();
	}
}
