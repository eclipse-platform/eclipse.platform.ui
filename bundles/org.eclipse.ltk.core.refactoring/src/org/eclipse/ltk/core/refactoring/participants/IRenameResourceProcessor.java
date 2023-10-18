/*******************************************************************************
* Copyright (c) 2020 IBM Corporation and others.
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
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.core.resources.IResource;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @since 3.11
 */
public interface IRenameResourceProcessor {

	/**
	 * Returns the resource this processor was created on
	 *
	 * @return the resource to rename
	 */
	IResource getResource();

	/**
	 * Returns the new resource name
	 *
	 * @return the new resource name
	 */
	String getNewResourceName();

	/**
	 * Sets the new resource name
	 *
	 * @param newName the new resource name
	 */
	void setNewResourceName(String newName);

	/**
	 * Returns <code>true</code> if the refactoring processor also updates references
	 *
	 * @return <code>true</code> if the refactoring processor also updates references
	 */
	boolean isUpdateReferences();

	/**
	 * Specifies if the refactoring processor also updates references.
	 *
	 * @param updateReferences <code>true</code> if the refactoring processor should also updates references
	 */
	void setUpdateReferences(boolean updateReferences);

	/**
	 * Validates if the a name is valid.
	 *
	 * @param newName the name to validate
	 * @return returns the resulting status of the validation
	 */
	RefactoringStatus validateNewElementName(String newName);
}
