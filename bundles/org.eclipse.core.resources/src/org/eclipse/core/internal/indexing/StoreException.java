/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.indexing;

import java.io.PrintStream;
import java.io.PrintWriter;

public abstract class StoreException extends Exception {
	protected Throwable wrappedException;

public StoreException(String message) {
	super(message);
}
public StoreException(String message, Throwable wrappedException) {
	super(message);
	this.wrappedException = wrappedException;
}
/**
 * Prints a stack trace out for the exception.
 */
public void printStackTrace() {
	printStackTrace(System.err);
}
/**
 * Prints a stack trace out for the exception.
 */
public void printStackTrace(PrintStream output) {
	synchronized (output) {
		super.printStackTrace(output);
		if (wrappedException != null)
			wrappedException.printStackTrace(output);
	}
}
/**
 * Prints a stack trace out for the exception.
 */
public void printStackTrace(PrintWriter output) {
	synchronized (output) {
		super.printStackTrace(output);
		if (wrappedException != null)
			wrappedException.printStackTrace(output);
	}
}

}

