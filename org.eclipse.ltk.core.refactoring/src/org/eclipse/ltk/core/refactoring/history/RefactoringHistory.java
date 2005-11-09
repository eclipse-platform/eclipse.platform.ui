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

/**
 * Object which represents a sequence of executed refactorings with time
 * information.
 * <p>
 * Refactoring histories are exposed by the refactoring history service as
 * result of queries and contain only lightweight proxy objects. The
 * refactoring history service may hand out any number of refactoring histories
 * and associated refactoring descriptor proxies for any given query.
 * </p>
 * <p>
 * Note: this class is not intended to be extended outside the refactoring framework.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public abstract class RefactoringHistory {

	/**
	 * Returns the refactoring descriptors of this history, in ascending order
	 * of their time stamps.
	 * 
	 * @return the refactoring descriptors, or an empty array
	 */
	public abstract RefactoringDescriptorProxy[] getDescriptors();

	/**
	 * Is the refactoring history empty?
	 * 
	 * @return <code>true</code> if the history is empty, <code>false</code>
	 *         otherwise
	 */
	public abstract boolean isEmpty();
}