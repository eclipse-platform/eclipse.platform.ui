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
package org.eclipse.ui.contexts;

/**
 * Signals that an attempt was made to access the properties of an undefined
 * object.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead.
 * @see org.eclipse.core.commands.common.NotDefinedException
 */
@Deprecated
public final class NotDefinedException extends ContextException {

	/**
	 * Generated serial version UID for this class.
	 *
	 * @since 3.1
	 */
	private static final long serialVersionUID = 3833750983926167092L;

	/**
	 * Creates a new instance of this class with the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public NotDefinedException(String message) {
		super(message);
	}

	/**
	 * Constructs a new instance of <code>NotDefinedException</code>.
	 *
	 * @param e The exception being thrown; must not be <code>null</code>.
	 * @since 3.1
	 */
	public NotDefinedException(org.eclipse.core.commands.common.NotDefinedException e) {
		super(e.getMessage(), e);
	}
}
