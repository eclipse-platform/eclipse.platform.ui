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
public class Gesture implements Comparable {

	public final static Gesture EAST = new Gesture("EAST"); 
	public final static Gesture NORTH = new Gesture("NORTH"); 
	public final static Gesture SOUTH = new Gesture("SOUTH"); 
	public final static Gesture WEST = new Gesture("WEST"); 
	
	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Gesture.class.getName().hashCode();

	private String name;

	private Gesture(String name) {
		super();
		
		if (name == null)
			throw new NullPointerException();
		
		this.name = name;
	}

	public int compareTo(Object object) {
		Gesture gesture = (Gesture) object;
		int compareTo = name.compareTo(gesture.name);
		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof Gesture))
			return false;

		Gesture gesture = (Gesture) object;
		return name.equals(gesture.name);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + name.hashCode();
		return result;
	}
	
	public String toString() {
		return name;
	}	
}
