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

import java.util.List;

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
public class GestureSequence {

	/**
	 * JAVADOC
	 * 
	 * @param gestureStrokes
	 * @return
	 */	
	public static GestureSequence create(List gestureStrokes) {
		return new GestureSequence(gestureStrokes);
	}

	private List gestureStrokes;
	
	private GestureSequence(List gestureStrokes) {
		super();
		this.gestureStrokes = gestureStrokes;
	}

	/**
	 * JAVADOC
	 * 
	 * @return
	 */
	public List getGestureStrokes() {
		return gestureStrokes;
	}
}
