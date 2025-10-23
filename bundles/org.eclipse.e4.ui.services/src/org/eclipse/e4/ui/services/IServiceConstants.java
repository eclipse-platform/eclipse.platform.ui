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
package org.eclipse.e4.ui.services;

/**
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 1.0
 */
public interface IServiceConstants {

	/**
	* The current selection
	* <p>
	* This value can be <code>null</code> if there is no selection
	* </p>
	*/
	public static final String ACTIVE_SELECTION = "org.eclipse.ui.selection"; //$NON-NLS-1$

	/**
	 * Due to the possibly misleading nature of this field's name, it has been
	 * replaced with {@link #ACTIVE_SELECTION}. All clients of this API should
	 * change their references to <code>ACTIVE_SELECTION</code>.
	 *
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated(forRemoval = true, since = "2025-12")
	public static final String SELECTION = ACTIVE_SELECTION;

	/**
	 * The set of the contexts that are currently active.
	 */
	public static final String ACTIVE_CONTEXTS = "activeContexts"; //$NON-NLS-1$

	/**
	 * The part active in a given context.
	 * <p>
	 * This value can be <code>null</code> if there is no active part in a given
	 * context.
	 * </p>
	 */
	public static final String ACTIVE_PART = "e4ActivePart"; //$NON-NLS-1$

	/**
	 * The currently active Shell.
	 */
	public static final String ACTIVE_SHELL = "activeShell"; //$NON-NLS-1$
}
