/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
