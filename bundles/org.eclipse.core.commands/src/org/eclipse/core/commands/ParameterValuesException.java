/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.commands;

import org.eclipse.core.commands.common.CommandException;

/**
 * <p>
 * Signals that a problem has occurred while trying to create an instance of
 * <code>IParameterValues</code>. In applications based on the registry
 * provided by core, this usually indicates a problem creating an
 * <code>IExecutableExtension</code>. For other applications, this exception
 * could be used to signify any general problem during initialization.
 * </p>
 * 
 * @since 3.1
 * 
 */
public final class ParameterValuesException extends CommandException {

	/**
	 * Generated serial version UID for this class.
	 */
	private static final long serialVersionUID = 3618976793520845623L;

	/**
	 * Creates a new instance of this class with the specified detail message
	 * and cause.
	 * 
	 * @param message
	 *            the detail message; may be <code>null</code>.
	 * @param cause
	 *            the cause; may be <code>null</code>.
	 */
	public ParameterValuesException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
