/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.history;

import org.eclipse.ltk.core.refactoring.RefactoringCore;

/**
 * Interface for refactoring execution listeners. Clients may register a
 * refactoring execution listener with the {@link IRefactoringHistoryService}
 * obtained by calling {@link RefactoringCore#getRefactoringHistoryService()} in
 * order to get informed about refactoring execution events.
 * <p>
 * Note: this interface is intended to be implemented by clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public interface IRefactoringExecutionListener {

	/**
	 * Gets called if a refactoring execution event happend.
	 * 
	 * @param event
	 *            the refactoring execution event
	 */
	public void executionNotification(RefactoringExecutionEvent event);
}