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
package org.eclipse.core.runtime;

/**
 * This exception is thrown to blow out of a long-running method 
 * when the user cancels it.
 */
public final class OperationCanceledException extends RuntimeException {
/**
 * Creates a new exception.
 */
public OperationCanceledException() {
	super();
}
/**
 * Creates a new exception with the given message.
 */
public OperationCanceledException(String message) {
	super(message);
}
}
