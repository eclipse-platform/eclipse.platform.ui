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

package org.eclipse.ui.internal.csm.commands.api;

/**
 * <p>
 * Signals that an attempt was made to access the attributes of an undefined
 * key configuration.
 * </p>
 * <p>
 * This class is not intended to be extended by clients.
 * </p> 
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public class KeyConfigurationNotDefinedException extends Exception {

	/**
	 * Constructs a <code>KeyConfigurationNotDefinedException</code> with no specified 
	 * detail message. 
	 */	
	public KeyConfigurationNotDefinedException() {
	}

	/**
	 * Constructs a <code>KeyConfigurationNotDefinedException</code> with the specified 
	 * detail message. 
	 *
	 * @param s	the detail message.
	 */	
	public KeyConfigurationNotDefinedException(String s) {
		super(s);
	}
}