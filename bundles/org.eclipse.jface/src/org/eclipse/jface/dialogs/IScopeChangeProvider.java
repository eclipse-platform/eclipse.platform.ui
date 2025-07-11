/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
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
 * @since 3.38
 *
 */
public interface IScopeChangeProvider {

	/**
	 * Adds a listener for scope changes in this scope change provider. Has no
	 * effect if an identical listener is already registered.
	 *
	 * @param listener a scope changed listener
	 */
	void addScopeChangedListener(IScopeChangedListener listener);

	/**
	 * Removes the given scope change listener from this page change provider. Has
	 * no effect if an identical listener is not registered.
	 *
	 * @param listener a scope changed listener
	 */
	void removeScopeChangedListener(IScopeChangedListener listener);

}
