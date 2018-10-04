/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/

package org.eclipse.urischeme.internal.registration;

/**
 * Exception class for handling exceptions occurring while accessing windows
 * registry
 *
 */
public class WinRegistryException extends Exception {

	private static final long serialVersionUID = 4601369218423588914L;

	@SuppressWarnings("javadoc")
	public WinRegistryException(String message) {
		super(message);
	}

	@SuppressWarnings("javadoc")
	public WinRegistryException(Throwable cause) {
		super(cause);
	}

	@SuppressWarnings("javadoc")
	public WinRegistryException(String message, Throwable cause) {
		super(message, cause);
	}

}
