/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

/** 
 * An unchecked exception indicating that an attempt to access
 * an extension registry object that is no longer valid.
 * <p>
 * This exception is thrown by methods on extension registry
 * objects. It is not intended to be instantiated or
 * subclassed by clients.
 * </p>
 * 
 * @since 3.1
 */
public class InvalidRegistryObjectException extends RuntimeException {
	/*
	 * Declare a stable serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "Invalid registry object"; //$NON-NLS-1$

	/**
	 * Creates a new exception instance with null as its detail message.
	 */
	public InvalidRegistryObjectException() {
		super(MESSAGE);
	}
}
