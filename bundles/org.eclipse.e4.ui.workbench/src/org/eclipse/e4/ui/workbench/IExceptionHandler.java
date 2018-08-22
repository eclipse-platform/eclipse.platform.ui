/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
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
package org.eclipse.e4.ui.workbench;

/**
 * This handler allows clients to be notified when an exception occurs
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 1.0
 */
public interface IExceptionHandler {

	/**
	 * Call-back to handle the given {@link Throwable}
	 *
	 * @param e
	 *            the {@link Throwable}
	 */
	public void handleException(Throwable e);
}
