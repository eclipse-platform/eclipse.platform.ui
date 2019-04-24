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
 * Signals that an attempt was made to access the properties of an unhandled
 * object.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead. This
 *             API is scheduled for deletion, see Bug 431177 for details
 * @see org.eclipse.core.commands.NotHandledException
 * @noreference This class is scheduled for deletion.
 */
@Deprecated
@SuppressWarnings("all")
public final class NotHandledException extends CommandException {

	/**
	 * Generated serial version UID for this class.
	 *
	 * @since 3.1
	 */
	private static final long serialVersionUID = 3256446914827726904L;

	/**
	 * Creates a new instance of this class with the specified detail message.
	 *
	 * @param s the detail message.
	 */
	@Deprecated
	public NotHandledException(String s) {
		super(s);
	}

	/**
	 * Constructs a legacy <code>NotHandledException</code> based on the new
	 * <code>NotHandledException</code>
	 *
	 * @param e The exception from which this exception should be created; must not
	 *          be <code>null</code>.
	 * @since 3.1
	 */
	@Deprecated
	public NotHandledException(final org.eclipse.core.commands.NotHandledException e) {
		super(e.getMessage(), e);
	}
}
