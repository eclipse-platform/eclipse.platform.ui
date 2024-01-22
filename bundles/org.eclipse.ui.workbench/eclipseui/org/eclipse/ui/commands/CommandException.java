/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.commands;

/**
 * Signals that an exception occurred within the command architecture.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead. This
 *             API is scheduled for deletion, see Bug 431177 for details
 * @see org.eclipse.core.commands.common.CommandException
 * @noextend This class is not intended to be subclassed by clients.
 * @noreference This class is scheduled for deletion.
 */
@Deprecated
@SuppressWarnings("all")
public abstract class CommandException extends Exception {

	/**
	 * Generated serial version UID for this class.
	 *
	 * @since 3.4
	 */
	private static final long serialVersionUID = 1776879459633730964L;

	private Throwable cause;

	/**
	 * Creates a new instance of this class with the specified detail message.
	 *
	 * @param message the detail message.
	 */
	@Deprecated
	public CommandException(String message) {
		super(message);
	}

	/**
	 * Creates a new instance of this class with the specified detail message and
	 * cause.
	 *
	 * @param message the detail message.
	 * @param cause   the cause.
	 */
	@Deprecated
	public CommandException(String message, Throwable cause) {
		super(message);
		// don't pass the cause to super, to allow compilation against JCL Foundation
		this.cause = cause;
	}

	/**
	 * Returns the cause of this throwable or <code>null</code> if the cause is
	 * nonexistent or unknown.
	 *
	 * @return the cause or <code>null</code>
	 * @since 3.1
	 */
	@Override
	@Deprecated
	public Throwable getCause() {
		return cause;
	}
}
