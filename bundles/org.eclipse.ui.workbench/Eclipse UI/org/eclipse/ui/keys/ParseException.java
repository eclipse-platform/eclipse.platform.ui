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

/**
 * <p>
 * This class represents the exception caused due to errors parsing of KeyStroke 
 * and KeySequence objects from strings.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public final class ParseException extends Exception {

	/**
	 * Constructs a <code>ParseException</code> with no specified detail 
	 * message. 
	 */	
	public ParseException() {
	}

	/**
	 * Constructs a <code>ParseException</code> with the specified detail 
	 * message. 
	 *
	 * @param s	the detail message.
	 */	
	public ParseException(final String s) {
		super(s);
	}
}
