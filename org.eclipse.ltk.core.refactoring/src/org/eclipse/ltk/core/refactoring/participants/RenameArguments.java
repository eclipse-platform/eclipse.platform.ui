/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Rename arguments describe the data that a processor
 * provides to its rename participants.
 * 
 * @since 3.0
 */
public class RenameArguments extends RefactoringArguments {
	
	private String fNewName;
	private boolean fUpdateReferences;
	
	/**
	 * Creates new rename arguments.
	 * 
	 * @param newName the new name of the element to be renamed
	 * @param updateReferences <code>true</code> if reference
	 *  updating is requested; <code>false</code> otherwise
	 */
	public RenameArguments(String newName, boolean updateReferences) {
		Assert.isNotNull(newName);
		fNewName= newName;
		fUpdateReferences= updateReferences;
	}
	
	/**
	 * Returns the new element name.
	 * 
	 * @return the new element name
	 */
	public String getNewName() {
		return fNewName;
	}
	
	/**
	 * Returns whether reference updating is requested or not.
	 * 
	 * @return returns <code>true</code> if reference
	 *  updating is requested; <code>false</code> otherwise
	 */
	public boolean getUpdateReferences() {
		return fUpdateReferences;
	}
}
