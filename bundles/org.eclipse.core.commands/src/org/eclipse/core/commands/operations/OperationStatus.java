/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
 * <p>
 * Note: This class/interface is part of a new API under development. It has
 * been added to builds so that clients can start using the new features.
 * However, it may change significantly before reaching stability. It is being
 * made available at this early stage to solicit feedback with the understanding
 * that any code that uses this API may be broken as the API evolves.
 * </p>
 * 
 * @since 3.1
 * @experimental
 */
public class OperationStatus extends Status {
	public static final int NOTHING_TO_REDO = 12;

	public static final int NOTHING_TO_UNDO = 11;

	public static final int OPERATION_INVALID = 10;

	public static String PLUGIN_ID = "org.eclipse.commands"; //$NON-NLS-1$

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
