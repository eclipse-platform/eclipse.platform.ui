/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.core.refactoring.history;

import org.eclipse.ltk.core.refactoring.RefactoringCore;

/**
 * Interface for refactoring history listeners.Clients may register a
 * refactoring history listener with the {@link IRefactoringHistoryService}
 * obtained by calling {@link RefactoringCore#getHistoryService()} in order to
 * get informed about refactoring history events.
 * <p>
 * Note: this interface is intended to be implemented by clients.
 * </p>
 *
 * @see IRefactoringHistoryService
 * @see RefactoringHistoryEvent
 *
 * @since 3.2
 */
public interface IRefactoringHistoryListener {

	/**
	 * Gets called if a refactoring history event happened.
	 * <p>
	 * Implementors of this method should not rely on a fixed set of event
	 * types.
	 * </p>
	 * <p>
	 * The event object is valid only for the duration of this method.
	 * </p>
	 *
	 * @param event
	 *            the refactoring history event
	 */
	void historyNotification(RefactoringHistoryEvent event);
}
