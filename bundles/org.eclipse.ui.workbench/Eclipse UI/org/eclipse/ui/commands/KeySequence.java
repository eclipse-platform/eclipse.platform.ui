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
public class KeySequence {

	/**
	 * JAVADOC
	 * 
	 * @param keyStrokes
	 * @return
	 */	
	public static KeySequence create(List keyStrokes) {
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
	public List getKeyStrokes() {
		return keyStrokes;
	}
}
