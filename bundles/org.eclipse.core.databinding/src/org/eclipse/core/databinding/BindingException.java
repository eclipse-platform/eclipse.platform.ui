/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.databinding;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * An unchecked exception indicating a binding problem.
 * 
 * @since 1.0
 */
public class BindingException extends RuntimeException {

	/*
	 * Needed because all Throwables are Serializable.
	 */
	private static final long serialVersionUID = -4092828452936724217L;
	private Throwable cause;

	/**
	 * Creates a new BindingException with the given message.
	 * 
	 * @param message
	 */
	public BindingException(String message) {
		super(message);
	}

	/**
	 * Creates a new BindingException with the given message and cause.
	 * 
	 * @param message
	 * @param cause
	 */
	public BindingException(String message, Throwable cause) {
		super(message);
		this.cause = cause;
	}

	public void printStackTrace(PrintStream err) {
		super.printStackTrace(err);
		if (cause != null) {
			err.println("caused by:"); //$NON-NLS-1$
			cause.printStackTrace(err);
		}
	}

	public void printStackTrace(PrintWriter err) {
		super.printStackTrace(err);
		if (cause != null) {
			err.println("caused by:"); //$NON-NLS-1$
			cause.printStackTrace(err);
		}
	}
}
