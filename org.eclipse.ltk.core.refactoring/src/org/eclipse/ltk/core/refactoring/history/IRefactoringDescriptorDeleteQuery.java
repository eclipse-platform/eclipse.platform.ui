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

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * A query interface to decide whether a refactoring descriptor may be deleted.
 * <p>
 * Clients should be aware that the methods defined by this interface can be
 * called from a non user interface thread.
 * </p>
 * <p>
 * Note: this interface is intended to be implemented by clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public interface IRefactoringDescriptorDeleteQuery {

	/**
	 * Returns whether the current delete operation can be executed.
	 * <p>
	 * The refactoring descriptor will considered to be deleted only if this
	 * method returns a status of severity {@link RefactoringStatus#OK}.<br>
	 * If a status of severity {@link RefactoringStatus#FATAL} is returned, the
	 * entire operation which this deletion if part of is considered to be
	 * aborted.
	 * </p>
	 * 
	 * @param proxy
	 *            the refactoring descriptor proxy to delete
	 * @return a refactoring status describing the outcome of the user prompt
	 */
	public RefactoringStatus proceed(RefactoringDescriptorProxy proxy);
}