/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands.operations;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * <p>
 * OperationStatus describes the status of a request to execute, undo, or redo
 * an operation.
 * </p>
 * 
 * @since 3.1
 */
public class OperationStatus extends Status {
	/**
	 * NOTHING_TO_REDO indicates there was no operation available for redo.
	 */
	public static final int NOTHING_TO_REDO = 1;

	/**
	 * NOTHING_TO_UNDO indicates there was no operation available for undo.
	 */
	public static final int NOTHING_TO_UNDO = 2;

	/**
	 * OPERATION_INVALID indicates that the operation available for undo or redo
	 * is not in a state to perform the undo or redo.
	 */
	public static final int OPERATION_INVALID = 3;

	/**
	 * PLUGIN_ID identifies the plugin reporting the status.
	 */
	public static String PLUGIN_ID = "org.eclipse.core.commands"; //$NON-NLS-1$

	/**
	 * Creates a new operation status, assigning the severity.
	 * 
	 * @param severity
	 *            the severity for the status
	 * @param code
	 *            the informational code for the status
	 * @param message
	 *            a human-readable message, localized to the current locale
	 */
	public OperationStatus(int severity, int code, String message) {
		super(severity, PLUGIN_ID, code, message, null);
	}

	/**
	 * Creates a new operation status for errors.
	 * 
	 * @param code
	 *            the error code for the status
	 * @param message
	 *            a human-readable message, localized to the current locale
	 * @param exception
	 *            a low-level exception, or <code>null</code> if not
	 *            applicable
	 */
	public OperationStatus(int code, String message, Throwable exception) {
		super(IStatus.ERROR, PLUGIN_ID, code, message, exception);
	}
}
