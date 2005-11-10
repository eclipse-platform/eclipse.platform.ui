/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Rename arguments describe the data that a processor
 * provides to its rename participants.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 * 
 * @since 3.0
 */
public class RenameArguments extends RefactoringArguments {
	
	private String fNewName;
	private boolean fUpdateReferences;
	private boolean fUpdateDerivedElements;
	
	/**
	 * Creates new rename arguments.
	 * 
	 * @param newName the new name of the element to be renamed
	 * @param updateReferences <code>true</code> if reference
	 *  updating is requested; <code>false</code> otherwise
	 */
	public RenameArguments(String newName, boolean updateReferences) {
		this(newName, updateReferences, false);
	}
	
	/**
	 * Creates new rename arguments.
	 * 
	 * @param newName the new name of the element to be renamed
	 * @param updateReferences <code>true</code> if reference
	 *  updating is requested; <code>false</code> otherwise
	 * @param updateDerivedElements <code>true</code> if updating
	 *  of derived elements is requested; <code>false</code> otherwise
	 *  
	 * @since 3.2
	 */
	public RenameArguments(String newName, boolean updateReferences, boolean updateDerivedElements) {
		Assert.isNotNull(newName);
		fNewName= newName;
		fUpdateReferences= updateReferences;
		fUpdateDerivedElements= updateDerivedElements;
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
	

	/**
	 * Returns whether updating of derived elements is requested or not.
	 * 
	 * @return <code>true</code> if updating of derived elements is
	 *  requested; <code>false</code> otherwise
	 *  
	 * @since 3.2
	 */
	public boolean getUpdateDerivedElements() {
		return fUpdateDerivedElements;
	}
	
	/* (non-Javadoc)
	 * @see RefactoringArguments#toString()
	 */
	public String toString() {
		return "rename to " + fNewName //$NON-NLS-1$
				+ (fUpdateReferences ? " (update references)" : " (don't update references)") //$NON-NLS-1$//$NON-NLS-2$
				+ (fUpdateDerivedElements ? " (update derived elements)" : " (don't update derived elements)"); //$NON-NLS-1$//$NON-NLS-2$
	}
}
