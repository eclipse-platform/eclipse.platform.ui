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
package org.eclipse.team.internal.core.target;

public class BaseIdentifierNotInitializedException extends Exception {
	/**
	 * Default constructor for a <code>TeamProviderException</code>.
	 */
	public BaseIdentifierNotInitializedException() {
		super();
	}

	/**
	 * Constructor for a <code>TeamProviderException</code> that takes
	 * a string description of the cause of the exception.
	 * 
 	 * @param message a message describing the cause of the exception.
	 */
	public BaseIdentifierNotInitializedException(String message) {
		super(message);
	}
}

