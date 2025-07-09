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
 * A listener which is notified when the scope for the search page is changed.
 *
 * @see IScopeChangeProvider
 * @see ScopeChangedEvent
 *
 * @since 3.38
 */
public interface IScopeChangedListener {
	/**
	 * Notifies that the selected scope has changed.
	 *
	 * @param event event object describing the change
	 */
	void scopeChanged(ScopeChangedEvent event);
}
