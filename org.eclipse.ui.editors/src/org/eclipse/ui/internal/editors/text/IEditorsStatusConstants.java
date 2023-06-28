/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
package org.eclipse.ui.internal.editors.text;

/**
 * Defines plug-in-specific status codes.
 *
 * @see org.eclipse.core.runtime.IStatus#getCode()
 * @see org.eclipse.core.runtime.Status#Status(int, java.lang.String, int, java.lang.String, java.lang.Throwable)
 * @since 2.1
 */
interface IEditorsStatusConstants {

	/**
	 * Status constant indicating that an internal error occurred.
	 * Value: <code>1001</code>
	 */
	public static final int INTERNAL_ERROR= 10001;

 }
