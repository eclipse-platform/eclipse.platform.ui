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
package org.eclipse.ltk.internal.core.refactoring.history;

import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

/**
 * Interface for refactorings which can be initialized by refactoring arguments.
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public interface IInitializableRefactoring {

	/**
	 * Initializes the refactoring with the refactoring arguments.
	 * 
	 * @param arguments
	 *            the refactoring arguments
	 * @return <code>true</code> if the refactoring could be initialized,
	 *         <code>false</code> otherwise. If the refactoring could not be
	 *         initialized, it will not be executed.
	 */
	public boolean initialize(RefactoringArguments arguments);
}
