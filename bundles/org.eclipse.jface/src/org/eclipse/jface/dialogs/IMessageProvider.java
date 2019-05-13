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
package org.eclipse.jface.dialogs;

/**
 * Minimal interface to a message provider. Used for dialog pages which can
 * provide a message with an icon.
 *
 * @since 2.0
 */
public interface IMessageProvider {
	/**
	 * Constant for a regular message (value 0).
	 * <p>
	 * Typically this indicates that the message should be shown without an
	 * icon.
	 * </p>
	 */
	public static final int NONE = 0;

	/**
	 * Constant for an info message (value 1).
	 */
	public static final int INFORMATION = 1;

	/**
	 * Constant for a warning message (value 2).
	 */
	public static final int WARNING = 2;

	/**
	 * Constant for an error message (value 3).
	 */
	public static final int ERROR = 3;

	/**
	 * Returns the current message for this message provider.
	 * <p>
	 * A message provides instruction or information to the user.
	 * </p>
	 *
	 * @return the message, or <code>null</code> if none
	 */
	public String getMessage();

	/**
	 * Returns a value indicating if the message is a an information message, a
	 * warning message, or an error message.
	 * <p>
	 * Returns one of <code>NONE</code>,<code>INFORMATION</code>,
	 * <code>WARNING</code>, or <code>ERROR</code>.
	 * </p>
	 *
	 * @return the message type
	 */
	public int getMessageType();
}
